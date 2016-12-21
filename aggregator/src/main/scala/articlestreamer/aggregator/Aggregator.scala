package articlestreamer.aggregator

import java.sql.Timestamp
import java.util.{TimeZone, UUID}

import articlestreamer.aggregator.kafka.scheduled.EndQueueJob
import articlestreamer.aggregator.redis.RedisClientFactory
import articlestreamer.aggregator.twitter.TwitterStreamerFactory
import articlestreamer.aggregator.twitter.utils.TwitterStatusMethods
import articlestreamer.shared.configuration.ConfigLoader
import articlestreamer.shared.kafka.{HalfDayTopicManager, KafkaProducerWrapper}
import articlestreamer.shared.marshalling.CustomJsonFormats
import articlestreamer.shared.model.{TweetAuthor, TwitterArticle}
import articlestreamer.shared.scoring.TwitterScoreCalculator
import com.typesafe.scalalogging.LazyLogging
import org.apache.kafka.clients.producer._
import org.json4s.jackson.Serialization.write
import org.quartz.CronScheduleBuilder._
import org.quartz.JobBuilder.newJob
import org.quartz.TriggerBuilder._
import org.quartz.{JobDataMap, Scheduler}
import twitter4j.Status

class Aggregator(config: ConfigLoader,
                 producer: KafkaProducerWrapper,
                 scheduler: Scheduler,
                 scoreCalculator: TwitterScoreCalculator,
                 streamer: TwitterStreamerFactory,
                 redisFactory: RedisClientFactory) extends CustomJsonFormats with TwitterStatusMethods with LazyLogging {

  val topicManager = new HalfDayTopicManager(config)
  val redisClient = redisFactory.getClient()

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

      scheduler.shutdown()
    })
  }

  private def tweetHandler(producer: KafkaProducerWrapper): (Status) => Unit = {
    (status: Status) => {

      logger.info(s"Status received: ${status.getCreatedAt}")

      if (status.isRetweet) {
        logger.warn(s"Tweet ${status.getId} ignored. Reason : 'Retweet' .")
      } else if (!status.containsEnglish) {
        logger.warn(s"Tweet ${status.getId} ignored. Reason : 'Not English' . Content : '${status.getText.mkString}'")
      } else if (!status.isPotentialArticle) {
        logger.warn(s"Tweet ${status.getId} ignored. Reason : 'Not Potential Article' . Content : '${status.getText.mkString}'")
      } else {
        val article = convertToArticle(status)

        val record = new ProducerRecord[String, String](
          topicManager.getCurrentTopic(),
          s"tweet-${status.getId}",
          write(article))

        producer.send(record)
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
      new Timestamp(status.getCreatedAt.getTime),
      urls,
      status.getText,
      TweetAuthor(author.getId, author.getScreenName, author.getFollowersCount),
      None)

    val baseScore = scoreCalculator.calculateBaseScore(article)
    article.copy(score = Some(baseScore))

  }

  private def scheduleEndOfQueue() = {

    val paramsMorning = new JobDataMap()
    paramsMorning.put("producer", producer)
    paramsMorning.put("topic", topicManager.getSecondTopic())
    val jobMorning = newJob(classOf[EndQueueJob]).withIdentity("terminateAfternoonQueue")
      .setJobData(paramsMorning)
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
      .setJobData(paramsAfternoon)
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
