package articlestreamer.shared

import org.scalamock.matchers.MatchAny
import org.scalatest.Matchers

trait AdditionalMatchers extends Matchers {

  case class ArgumentCaptor[T]() {
    var valueCaptured: Option[T] = None
  }

  class MatchAnyWithCaptor[T](captor: ArgumentCaptor[T]) extends MatchAny {
    override def equals(that: Any): Boolean = {
      captor.valueCaptured = Some(that.asInstanceOf[T])
      super.equals(that)
    }
  }

  def capture[T](captor: ArgumentCaptor[T]) = new MatchAnyWithCaptor[T](captor)
}
