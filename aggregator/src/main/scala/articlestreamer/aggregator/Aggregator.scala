package articlestreamer.aggregator

import java.sql.Date
import java.util.{TimeZone, UUID}

import articlestreamer.aggregator.kafka.KafkaProducerWrapper
import articlestreamer.aggregator.kafka.scheduled.EndQueueJob
import articlestreamer.aggregator.twitter.TwitterStreamerFactory
import articlestreamer.aggregator.twitter.utils.TwitterStatusMethods
import articlestreamer.shared.configuration.ConfigLoader
import articlestreamer.shared.kafka.HalfDayTopicManager
import articlestreamer.shared.marshalling.CustomJsonFormats
import articlestreamer.shared.model.{TweetAuthor, TwitterArticle}
import articlestreamer.shared.scoring.TwitterScoreCalculator
import com.typesafe.scalalogging.LazyLogging
import org.apache.kafka.clients.producer._
import org.json4s.jackson.Serialization.write
import org.quartz.CronScheduleBuilder._
import org.quartz.JobBuilder.newJob
import org.quartz.JobDataMap
import org.quartz.TriggerBuilder._
import org.quartz.impl.StdSchedulerFactory
import twitter4j.Status

class Aggregator(config: ConfigLoader,
                 producer: KafkaProducerWrapper,
                 scoreCalculator: TwitterScoreCalculator,
                 streamer: TwitterStreamerFactory) extends CustomJsonFormats with TwitterStatusMethods with LazyLogging {

  val topicManager = new HalfDayTopicManager(config)

  def run() = {

    scheduleEndOfQueue()

    val twitterStreamer = streamer.getStreamer(config, tweetHandler(producer), stopHandler(producer))

    logger.info("Starting streaming")
    twitterStreamer.startStreaming()

    sys.addShutdownHook({
      logger.info("Stopping streaming")
      twitterStreamer.stop()
      logger.info("Streaming stopped")

      producer.stopProducer()
    })
  }

  private def tweetHandler(producer: KafkaProducerWrapper): (Status) => Unit = {
    (status: Status) => {

      logger.info(s"Status received: ${status.getCreatedAt}")

      if (!status.isRetweet && status.isPotentialArticle) {
        val article = convertToArticle(status)

        val record = new ProducerRecord[String, String](
          topicManager.getCurrentTopic(),
          s"tweet-${status.getId}",
          write(article))

        producer.send(record)

      } else {
        logger.warn(s"Tweet ${status.getId} ignored : '${status.getText.mkString}'")
      }
    }
  }

  private def convertToArticle(status: Status): TwitterArticle = {

    val author = status.getUser

    val urls: List[String] = status.getURLEntities.map{
      urlEntity => urlEntity.getExpandedURL
    }.toList

    val article = TwitterArticle(
      UUID.randomUUID().toString,
      String.valueOf(status.getId),
      new Date(status.getCreatedAt.getTime),
      urls,
      status.getText,
      TweetAuthor(author.getId, author.getScreenName, author.getFollowersCount),
      None)

    val baseScore = scoreCalculator.calculateBaseScore(article)
    article.copy(score = Some(baseScore))

  }

  private def scheduleEndOfQueue() = {
    val scheduler = StdSchedulerFactory.getDefaultScheduler

    val paramsMorning = new JobDataMap()
    paramsMorning.put("producer", producer)
    paramsMorning.put("topic", topicManager.getSecondTopic())
    val jobMorning = newJob(classOf[EndQueueJob]).withIdentity("terminateAfternoonQueue")
      .setJobData(new JobDataMap())
      .build()
    val morningTrigger = newTrigger()
      .withIdentity("triggerTerminateAfternoonQueue")
      .withSchedule(dailyAtHourAndMinute(0, 1).inTimeZone(TimeZone.getTimeZone("UTC")))
      .forJob("terminateAfternoonQueue")
      .build()
    scheduler.scheduleJob(jobMorning, morningTrigger)

    val paramsAfternoon = new JobDataMap()
    paramsAfternoon.put("producer", producer)
    paramsAfternoon.put("topic", topicManager.getFirstTopic())
    val jobAfternoon = newJob(classOf[EndQueueJob]).withIdentity("terminateMorningQueue")
      .setJobData(new JobDataMap())
      .build()
    val afternoonTrigger = newTrigger()
      .withIdentity("triggerTerminateMorningQueue")
      .withSchedule(dailyAtHourAndMinute(12, 1).inTimeZone(TimeZone.getTimeZone("UTC")))
      .forJob("terminateMorningQueue")
      .build()
    scheduler.scheduleJob(jobAfternoon, afternoonTrigger)

    scheduler.start()
  }

  private def stopHandler(producer: KafkaProducerWrapper): () => Unit = {
    () => producer.stopProducer()
  }

}
