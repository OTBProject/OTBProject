package io.github.otbproject.otb.core.message.in.filter

import scala.concurrent.Promise
import scala.util.control.ControlThrowable

final class FilteredMessageException private[core](resultPromise: Promise[FilterResult]) extends ControlThrowable {
  def result = resultPromise.future
}
