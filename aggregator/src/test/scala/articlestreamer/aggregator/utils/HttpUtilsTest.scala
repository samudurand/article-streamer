package articlestreamer.aggregator.utils

import articlestreamer.shared.BaseSpec
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito.{mock, when}
import org.scalatest.BeforeAndAfter

import scalaj.http.HttpOptions.HttpOption
import scalaj.http.{BaseHttp, HttpRequest, HttpResponse}

class HttpUtilsTest extends BaseSpec with BeforeAndAfter {

  class HttpMock extends BaseHttp

  var http: BaseHttp = _
  var httpUtils: HttpUtils = _

  before {
    http = mock(classOf[HttpMock])
    httpUtils = new HttpUtils(http)
  }

  "An URL with a single redirections" should "be followed till the end" in {
    val simpleResponse = new HttpResponse[String]("", 200, Map())
    val redirectResponse = new HttpResponse[String]("", 304, Map("Location" -> IndexedSeq("http://final")))
    val request = mock(classOf[HttpRequest])
    when(request.options(any[HttpOption](), any())).thenReturn(request)
    when(request.asString).thenReturn(redirectResponse, simpleResponse)
    when(http.apply(anyString())).thenReturn(request)

    val endUrl = httpUtils.getEndUrl("http://redirection")

    endUrl shouldBe "http://final"
  }

}
