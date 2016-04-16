package io.github.otbproject.otb.plugin.test

import io.github.otbproject.otb.plugin.base.{Dependency, Plugin, PluginIdentifier, PluginInitializer}

class PSharedName1Info extends TestPluginInfo {
  override type P = PSharedName1
  override val identifier: PluginIdentifier[P] = PluginIdentifier(classOf[P], "Shared Name")

  override def createPlugin(initializer: PluginInitializer): P = new PSharedName1(initializer, this)
  override val requiredDependencies: Set[Dependency[_ <: Plugin]] = Set()
}
