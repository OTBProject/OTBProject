package io.github.otbproject.otb.plugin.test

import io.github.otbproject.otb.plugin.base.{Dependency, Plugin, PluginIdentifier, PluginInitializer}

class PDepChain0Info extends TestPluginInfo {
  override type P = PDepChain0
  override val identifier: PluginIdentifier[P] = PluginIdentifier(classOf[P], classOf[P].getSimpleName)

  override def createPlugin(initializer: PluginInitializer): P = new PDepChain0(initializer, this)

  val dep = Helper.dependency(classOf[PSimpleValid])

  override val requiredDependencies: Set[Dependency[_ <: Plugin]] = Set(dep)
}
