package articlestreamer.aggregator.utils

import scalaj.http.{BaseHttp, HttpOptions, HttpResponse}

class HttpUtils(http: BaseHttp) {

  /**
    * Follows a redirection link in a recursive manner until reaching the final URL
    * @param link original URL
    * @return the final URL reached
    */
  def getEndUrl(link: String): String = {

    def getNextUrl(link: String): String = {
      val response: HttpResponse[String] = http(link).options(HttpOptions.followRedirects(false)).asString
      val redirectUrl = response.header("Location")
      if (isRedirectCode(response.code) && redirectUrl.isDefined && redirectUrl.get.trim().nonEmpty) {
        getNextUrl(redirectUrl.get)
      } else {
        link
      }
    }

    getNextUrl(link)
  }

  def isRedirectCode(code: Int): Boolean = {
    code >= 300 && code < 400
  }

}
