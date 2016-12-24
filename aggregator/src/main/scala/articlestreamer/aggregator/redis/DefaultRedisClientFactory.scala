package articlestreamer.aggregator.redis
import com.redis.RedisClient

class DefaultRedisClientFactory extends RedisClientFactory {
  override def getClient(host: String, port: Int): RedisClient = new RedisClient(host, port)
}
