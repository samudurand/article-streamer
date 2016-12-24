package articlestreamer.aggregator.service

import articlestreamer.aggregator.redis.RedisClientFactory
import articlestreamer.shared.configuration.ConfigLoader
import com.redis.Seconds

/**
  * Uses Redis as URL Store
  */
class RedisURLStoreService(config: ConfigLoader, factory: RedisClientFactory) extends URLStoreService {

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
    redisClient.set(url, "", onlyIfExists = false, Seconds(config.redisConfig.expiryTime))
  }

}
