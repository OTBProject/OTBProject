package io.github.otbproject.otb.core

import io.github.otbproject.otb.misc.ThreadUtil
import com.google.common.eventbus.{AsyncEventBus, EventBus}
import org.apache.logging.log4j.{LogManager, Logger}

class Core {
  private[otb] val logger: Logger = LogManager.getLogger()
  val eventBus: EventBus = new AsyncEventBus("Main Event Bus", ThreadUtil.newCachedThreadPool("Event Bus %d"))
}

object Core {
  val core: Core = new Core
}
