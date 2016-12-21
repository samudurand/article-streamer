package articlestreamer.aggregator.redis

import com.redis.RedisClient

trait RedisClientFactory {

  def getClient(host: String, port: Int): RedisClient

}
