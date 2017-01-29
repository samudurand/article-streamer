package articlestreamer.aggregator.service

import articlestreamer.aggregator.redis.RedisClientFactory
import articlestreamer.shared.BaseSpec
import articlestreamer.shared.configuration.ConfigLoader
import com.redis.{RedisClient, SecondsOrMillis}
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfter

class RedisURLStoreServiceSpec extends BaseSpec with BeforeAndAfter {

  class TestConfig extends ConfigLoader

  var factory: RedisClientFactory = _

  before {
    factory = mock(classOf[RedisClientFactory])
  }

  "Store Service" should "find a URL as key from Redis" in {
    val client = mock(classOf[RedisClient])
    when(client.get("http://url")).thenReturn(Some(""))
    when(factory.getClient(any(), any())).thenReturn(client)

    val storeService = new RedisURLStoreService(new TestConfig, factory)
    val result = storeService.exists("http://url")

    result shouldBe true
    verify(client, times(1)).get("http://url")
  }

  "Store Service" should "not find an unknown URL in Redis" in {
    val client = mock(classOf[RedisClient])
    when(client.get("http://url")).thenReturn(None)
    when(factory.getClient(any(), any())).thenReturn(client)

    val storeService = new RedisURLStoreService(new TestConfig, factory)
    val result = storeService.exists("http://url")

    result shouldBe false
    verify(client, times(1)).get("http://url")
  }

  it should "save a new URL into Redis" in {
    val client = mock(classOf[RedisClient])
    when(client.set(ArgumentMatchers.eq("http://url"), ArgumentMatchers.eq(""),
      ArgumentMatchers.eq(false), any[SecondsOrMillis]())).thenReturn(true)
    when(factory.getClient(any(), any())).thenReturn(client)

    val storeService = new RedisURLStoreService(new TestConfig, factory)
    storeService.save("http://url")

    verify(client, times(1)).set(any(), any(), any(), any())
  }

  // Coverage purposes
  it should "fail to save a new URL into Redis" in {
    val client = mock(classOf[RedisClient])
    when(client.set(ArgumentMatchers.eq("http://url"), ArgumentMatchers.eq(""),
      ArgumentMatchers.eq(false), any[SecondsOrMillis]())).thenReturn(false)
    when(factory.getClient(any(), any())).thenReturn(client)

    val storeService = new RedisURLStoreService(new TestConfig, factory)
    storeService.save("http://url")

    verify(client, times(1)).set(any(), any(), any(), any())
  }

}
