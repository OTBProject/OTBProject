package io.github.otbproject.otb.plugin.test

import io.github.otbproject.otb.plugin.base.{Dependency, Plugin, PluginIdentifier, PluginInitializer}

class PSharedName0Info extends TestPluginInfo {
  override type P = PSharedName0
  override val identifier: PluginIdentifier[P] = PluginIdentifier(classOf[P], "Shared Name")

  override def createPlugin(initializer: PluginInitializer): P = new PSharedName0(initializer, this)
  override val requiredDependencies: Set[Dependency[_ <: Plugin]] = Set()
}
