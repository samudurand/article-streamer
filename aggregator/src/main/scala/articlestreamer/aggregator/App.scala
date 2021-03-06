package articlestreamer.aggregator

import articlestreamer.aggregator.redis.DefaultRedisClientFactory
import articlestreamer.aggregator.service.RedisURLStoreService
import articlestreamer.aggregator.twitter.DefaultTwitterStreamerFactory
import articlestreamer.aggregator.utils.HttpUtils
import articlestreamer.shared.configuration.DefaultConfigLoader
import articlestreamer.shared.kafka.{KafkaFactory, KafkaProducerWrapper}
import articlestreamer.shared.scoring.NaiveTwitterScoreCalculator
import articlestreamer.shared.twitter.service.TwitterService
import com.softwaremill.macwire._
import org.quartz.impl.StdSchedulerFactory
import twitter4j.TwitterFactory

/**
  * Created by sam on 15/10/2016.
  */
object App extends App {

  lazy val twitterFactory = wire[TwitterFactory]
  lazy val config = wire[DefaultConfigLoader]
  lazy val producerFactory = wire[KafkaFactory[String, String]]
  lazy val producer = wire[KafkaProducerWrapper]
  lazy val streamFactory = new DefaultTwitterStreamerFactory
  lazy val twitterService = wire[TwitterService]
  lazy val scoreCalculator = wire[NaiveTwitterScoreCalculator]
  lazy val redisFactory = wire[DefaultRedisClientFactory]
  lazy val urlStore = wire[RedisURLStoreService]
  lazy val http = scalaj.http.Http
  lazy val httpUtil = wire[HttpUtils]

  lazy val scheduler = StdSchedulerFactory.getDefaultScheduler

  val aggregator = wire[Aggregator]
  aggregator.run()

}
