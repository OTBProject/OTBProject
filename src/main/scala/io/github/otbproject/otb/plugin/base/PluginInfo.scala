package io.github.otbproject.otb.plugin.base

import io.github.otbproject.otb.misc.CoreVersion

trait PluginInfo {
  type P <: Plugin

  val identifier: PluginIdentifier[P]

  val version: CoreVersion

  def createPlugin(initializer: PluginInitializer): P

  /**
    * Returns a [[Set]] of dependencies which are required in order for the
    * plugin to be loaded.
    *
    * It is recommended that all members of the returned set also be stored
    * as instance values, so that they can be used to retrieve a reference
    * to the relevant [[Plugin]] instance without any casting by calling
    * [[Plugin.getRequiredDependencyInstance()]].
    *
    * It is also recommended to store as instance values any dependencies
    * which are not required, but which may be utilized to provide additional
    * functionality if present. These values can be used to retrieve an
    * [[Option]] of the plugin instance if it is present (also without any
    * casting) by calling [[Plugin.getOptionalDependencyInstance()]].
    *
    * @return a set of dependencies required in order for the plugin to be
    *         loaded
    */
  def requiredDependencies: Set[Dependency[_ <: Plugin]]
}
