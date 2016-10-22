package articlestreamer.shared.twitter.service

import java.util

import articlestreamer.shared.BaseSpec
import articlestreamer.shared.configuration.ConfigLoader
import articlestreamer.shared.model.TweetPopularity
import org.mockito.ArgumentCaptor
import org.mockito.Mockito._
import org.mockito.ArgumentMatchers._
import org.mockito.AdditionalAnswers._
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import twitter4j._

class TwitterServiceSpec extends BaseSpec {

  class TestConfig extends ConfigLoader
  val config = new TestConfig

  var factory: TwitterFactory = _
  var twitter: Twitter = _

  before {
    twitter = mock(classOf[Twitter])
    factory = mock(classOf[TwitterFactory])
    when(factory.getInstance).thenReturn(twitter)
  }

  it should "retrieve details of several tweets" in {
    val tweetsIds = List(1l, 2l, 3l)
    val status1 = buildStatus(1l, 10, 0)
    val status2 = buildStatus(2l, 0, 20)
    val status3 = buildStatus(3l, 10, 20)

    val list = util.Arrays.asList(status1, status2, status3)
    val statusList = mock(classOf[ResponseList[Status]])
    when(statusList.forEach(any())).thenAnswer(delegatesTo(list))
    when(statusList.size()).thenAnswer(delegatesTo(list))

    val idCaptor: ArgumentCaptor[Long] = ArgumentCaptor.forClass(classOf[Long])
    when(twitter.lookup(idCaptor.capture())).thenReturn(statusList)

    val twitterService = new TwitterService(config, factory)
    val results = twitterService.getTweetsPopularities(tweetsIds)

    idCaptor.getAllValues should contain inOrderOnly (1l, 2l, 3l)
    results should have size 3
    results should contain (1l -> Some(TweetPopularity(10, 0)))
    results should contain (2l -> Some(TweetPopularity(0, 20)))
    results should contain (3l -> Some(TweetPopularity(10, 20)))
  }

  it should "retrieve return empty popularity details if retrieval fail" in {
    val tweetsIds = List(1l, 2l, 3l)
    val status1 = buildStatus(1l, 10, 0)
    val status3 = buildStatus(3l, 10, 20)

    val list = util.Arrays.asList(status1, status3)
    val statusList = mock(classOf[ResponseList[Status]])
    when(statusList.forEach(any())).thenAnswer(delegatesTo(list))

    val idCaptor: ArgumentCaptor[Long] = ArgumentCaptor.forClass(classOf[Long])
    when(twitter.lookup(idCaptor.capture())).thenReturn(statusList)

    val twitterService = new TwitterService(config, factory)
    val results = twitterService.getTweetsPopularities(tweetsIds)

    idCaptor.getAllValues should contain inOrderOnly (1l, 2l, 3l)
    results should have size 3
    results should contain (1l -> Some(TweetPopularity(10, 0)))
    results should contain (3l -> Some(TweetPopularity(10, 20)))
    results should contain (2l -> None)
  }

  it should "return no results if an exception occurs" in {
    val tweetsIds = List(1l, 2l, 3l)

    when(twitter.lookup(any())).thenThrow(new RuntimeException)

    val twitterService = new TwitterService(config, factory)
    val results = twitterService.getTweetsPopularities(tweetsIds)

    results shouldBe empty
  }

  def buildStatus(id: Long, rtCount: Int, favCount: Int): Status = {
    val status = mock(classOf[Status])
    when(status.getId).thenReturn(id)
    when(status.getFavoriteCount).thenReturn(favCount)
    when(status.getRetweetCount).thenReturn(rtCount)
    status
  }

}
