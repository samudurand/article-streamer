package articlestreamer.aggregator

import java.sql.Timestamp
import java.util.{TimeZone, UUID}

import articlestreamer.aggregator.kafka.scheduled.EndQueueJob
import articlestreamer.aggregator.service.URLStoreService
import articlestreamer.aggregator.twitter.TwitterStreamerFactory
import articlestreamer.aggregator.twitter.utils.TwitterStatusMethods
import articlestreamer.aggregator.utils.HttpUtils
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
import twitter4j.{Status, URLEntity}

import scalaj.http.BaseHttp

class Aggregator(http: BaseHttp,
                 config: ConfigLoader,
                 producer: KafkaProducerWrapper,
                 scheduler: Scheduler,
                 scoreCalculator: TwitterScoreCalculator,
                 streamer: TwitterStreamerFactory,
                 httpUtils: HttpUtils,
                 urlStore: URLStoreService) extends CustomJsonFormats with TwitterStatusMethods with LazyLogging {

  private val topicManager = new HalfDayTopicManager(config)
  private val ignoredAuthors = config.twitterConfig.ignoredAuthors.map(username => username.toLowerCase)

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

      scheduler.shutdown(false)
    })
  }

  private def tweetHandler(producer: KafkaProducerWrapper): (Status) => Unit = {
    (status: Status) => {

      logger.info(s"Status received: ${status.getCreatedAt}")

      if (status.isRetweet) {
        logger.warn(s"Tweet ${status.getId} ignored. Reason : 'Retweet' .")
      } else if (!status.containsEnglish) {
        logger.warn(s"Tweet ${status.getId} ignored. Reason : 'Not English' . Content : '${status.getText.mkString}'")
      } else if (ignoredAuthors.contains(status.getUser.getScreenName.toLowerCase)) {
        logger.warn(s"Tweet ${status.getId} ignored. Reason : 'Ignored author' . Author : '${status.getUser.getScreenName.mkString}'")
      } else {
        val usableLinks = status.getUsableLinks(config.twitterConfig.ignoredDomains)
        if (usableLinks.isEmpty) {
          logger.warn(s"Tweet ${status.getId} ignored. Reason : 'Not Potential Article' . Content : '${status.getText.mkString}'")
        } else {
          val unknownLinks = findUnknownLinks(usableLinks)
          if (unknownLinks.isEmpty) {
            logger.warn(s"Tweet ${status.getId} ignored. Reason : 'Links all already known or broken shortlink' . Links : '${status.getURLEntities.mkString}'")
          } else {
            saveNewLinks(unknownLinks)

            val article = convertToArticle(status)

            val record = new ProducerRecord[String, String](
              topicManager.getCurrentTopic(),
              s"tweet-${status.getId}",
              write(article))

            producer.send(record)
          }
        }
      }
    }
  }

  private def saveNewLinks(links: Seq[String]) = {
    links.foreach { link =>
      urlStore.save(link)
    }
  }

  /**
    * Check if the provided list contains at least one link unknown by interrogating the Link Storage.
    * Any shortlink is explored to discover the real URL and discarded is leading to a broken link.
    */
  private def findUnknownLinks(links: Seq[URLEntity]): Seq[String] = {
    links.flatMap { link =>
      val url = link.getExpandedURL
      if (httpUtils.isPotentialShortLink(url)) {
        httpUtils.getEndUrl(url)
      } else {
        Some(url)
      }
    }.filterNot { link =>
      urlStore.exists(link)
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
