package io.github.otbproject.otb.plugin.base

import com.google.common.eventbus.EventBus
import org.apache.logging.log4j.Logger

final case class PluginInitializer private[plugin](private[base] val logger: Logger,
                                                   private[base] val eventBus: EventBus,
                                                   private[base] val dependencyMap: Map[Dependency[_ <: Plugin], Plugin])
