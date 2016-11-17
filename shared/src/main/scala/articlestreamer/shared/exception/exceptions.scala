package articlestreamer.shared.exception

import java.io.{PrintWriter, StringWriter}


object exceptions {

  implicit class ExceptionUtils(ex: Throwable) {

    def getStackTraceAsString = {
      val sw = new StringWriter
      ex.printStackTrace(new PrintWriter(sw))
      sw.toString
    }

  }

}
