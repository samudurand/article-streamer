package articlestreamer.aggregator.service

import articlestreamer.aggregator.redis.RedisClientFactory
import articlestreamer.shared.configuration.ConfigLoader
import com.redis.Seconds
import com.typesafe.scalalogging.LazyLogging

/**
  * Uses Redis as URL Store
  */
class RedisURLStoreService(config: ConfigLoader, factory: RedisClientFactory) extends URLStoreService with LazyLogging {

  private val redisClient = factory.getClient(config.redisConfig.host, config.redisConfig.port)

  sys.addShutdownHook {
    redisClient.disconnect
  }

  override def exists(url: String): Boolean = {
    redisClient.get[String](url).isDefined
  }

  /**
    * Save a new URL in the store
    */
  override def save(url: String): Unit = {
    val result = redisClient.set(url, "", onlyIfExists = false, Seconds(config.redisConfig.expiryTime))
    if (result) {
      logger.info(s"Redis: successfully saved url [$url]")
    } else {
      logger.error(s"Redis: failed to save url [$url]")
    }
  }

}
