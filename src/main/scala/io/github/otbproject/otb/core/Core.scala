package io.github.otbproject.otb.core

import org.apache.logging.log4j.{LogManager, Logger}

class Core {
  private[otb] val logger: Logger = LogManager.getLogger()
}

object Core {
  val core: Core = new Core
}
