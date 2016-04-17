package io.github.otbproject.otb.core.message.in

import io.github.otbproject.otb.core.ChannelUser

// TODO: add timestamp field?
final case class MessageIn(source: MessageSource, user: ChannelUser, text: String) {
  /**
    * The text of the message split on spaces, with duplicate spaces removed.
    */
  lazy val tokenizedText = text.split(" ").toStream.filterNot(_.isEmpty).toList
}
