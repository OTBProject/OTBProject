package io.github.otbproject.otb.core.message.in.filter

import io.github.otbproject.otb.core.message.in.MessageIn

trait MessageFilter {
  /**
    * Return an [[Option]] of a [[FilterResult]] describing how a
    * message was filtered if it was, or an empty Option otherwise.
    *
    * @param message the message to filter
    * @return an Option containing the result of the filter operation
    */
  def apply(message: MessageIn): Option[FilterResult]
}
