package articlestreamer.aggregator.utils

import com.typesafe.scalalogging.LazyLogging

import scalaj.http.{BaseHttp, HttpOptions, HttpRequest, HttpResponse}

class HttpUtils(http: BaseHttp) extends LazyLogging {

  /**
    * Follows a redirection link in a recursive manner until reaching the final URL
    * @param link original URL
    * @return the final URL reached or None if an error code was returned on the way
    */
  def getEndUrl(link: String): Option[String] = {

    def getNextUrl(link: String): Option[String] = {
      val request: HttpRequest = http(link).options(HttpOptions.followRedirects(false))
      try {
        val response = request.asString
        val redirectUrl = response.header("Location")
        val responseCode = response.code
        if (responseCode >= 400) {
          None
        } else if (isRedirectCode(responseCode) && redirectUrl.isDefined && redirectUrl.get.trim().nonEmpty) {
          getNextUrl(redirectUrl.get)
        } else {
          Some(link)
        }
      } catch {
        case ex: Throwable =>
          logger.warn(s"Exception when trying to follow link $link")
          None
      }
    }

    getNextUrl(link)
  }

  def isRedirectCode(code: Int): Boolean = {
    code >= 300 && code < 400
  }

  /**
    * Find out very naively if a link is likely to be a shortlink
    * TODO at the moment only based on link length
    */
  def isPotentialShortLink(link: String): Boolean = {
    link.length < 50
  }

}
