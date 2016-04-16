package io.github.otbproject.otb.plugin.test

import io.github.otbproject.otb.plugin.base.{Dependency, Plugin, PluginIdentifier, PluginInitializer}

class PBadInstantiatorInfo extends TestPluginInfo {
  override type P = PBadInstantiator
  override val identifier: PluginIdentifier[P] = PluginIdentifier(classOf[P], classOf[P].getSimpleName)

  override def createPlugin(initializer: PluginInitializer): P = new PBadInstantiator(initializer, this)

  override val requiredDependencies: Set[Dependency[_ <: Plugin]] = Set()
}
