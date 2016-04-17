package io.github.otbproject.otb.core.event

import io.github.otbproject.otb.core.message.in.MessageIn
import io.github.otbproject.otb.core.message.in.filter.FilteredMessageException

import scala.concurrent.Future

/**
  * The filtered parameter is a [[Future]] indicating whether or not
  * the message was filtered. If the message is not filtered, the future
  * will succeed; if the message is filtered, the future will be failed
  * with a [[FilteredMessageException]]. The FilteredMessageException
  * can be used to determine how strongly the message was filtered
  * (e.g. a warning, ban, etc.).
  *
  * @param message
  * @param filtered
  */
final case class IncomingMessageEvent private[core](message: MessageIn, filtered: Future[Unit])
