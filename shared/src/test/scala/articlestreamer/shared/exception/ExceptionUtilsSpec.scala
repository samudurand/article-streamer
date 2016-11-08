package articlestreamer.shared.exception

import articlestreamer.shared.BaseSpec
import articlestreamer.shared.exception.exceptions._

class ExceptionUtilsSpec extends BaseSpec {

  "Utils" should "format exception stacktrace" in {
    val ex = new RuntimeException
    ex.getStackTraceAsString should startWith regex "java.lang.RuntimeException"
  }

  //For coverage purpose
  "Utils" should "print neat stacktrace" in {
    val ex = new RuntimeException
    ex.printNeatStackTrace()
  }

}
