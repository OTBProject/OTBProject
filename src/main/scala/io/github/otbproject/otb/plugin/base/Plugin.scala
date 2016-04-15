package io.github.otbproject.otb.plugin.base

import com.google.common.eventbus.EventBus
import org.apache.logging.log4j.Logger

abstract class Plugin private[plugin](initializer: PluginInitializer) {
  type Info <: PluginInfo

  /**
    * TODO: finish description
    *
    * Note: This MUST be the same PluginInfo instance which was used to
    * instantiate this plugin.
    */
  val info: Info

  /**
    * This method is called when the plugin (and all other plugins) are
    * finished loading. Override it to perform some action after it is
    * loaded.
    */
  protected def onLoad(): Unit = {}

  /**
    * This method is called prior to unloading this plugin. Override it to
    * perform some action before it is unloaded.
    */
  protected def onUnload(): Unit = {}

  /**
    * The [[EventBus]] which is used to broadcast and subscribe to all
    * built-in events.
    *
    * All plugins have the same EventBus instance; this method is merely for
    * convenience.
    */
  final val eventBus: EventBus = initializer.eventBus

  /**
    * The [[Logger]] specific to this plugin.
    *
    * It is recommended to create an accessor method (e.g. 'logger()') which
    * is private to the scope of the plugin (can only be called from the
    * plugin's code, but not from outside it).
    */
  protected final val pluginLogger: Logger = initializer.logger

  @throws[InvalidDependencyException]
  final def getRequiredDependencyInstance[P <: Plugin](dependency: Dependency[P]): P = {
    val option = initializer.dependencyMap.get(dependency)
    if (option.isEmpty) {
      throw new InvalidDependencyException(dependency.identifier)
    }
    dependency.identifier.pClass.cast(option.get)
  }

  final def getOptionalDependencyInstance[P <: Plugin](dependency: Dependency[P]): Option[P] = {
    ??? // TODO: impl
  }

  /* For encapsulation purposes */

  private[plugin] def callOnLoad() = onLoad()

  private[plugin] def callOnUnload() = onUnload()
}
