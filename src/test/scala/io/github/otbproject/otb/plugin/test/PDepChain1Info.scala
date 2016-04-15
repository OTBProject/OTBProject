package io.github.otbproject.otb.plugin.test

import io.github.otbproject.otb.plugin.base.{Dependency, Plugin, PluginIdentifier, PluginInitializer}

class PDepChain1Info extends TestPluginInfo {
  override type P = PDepChain1
  override val identifier: PluginIdentifier[P] = PluginIdentifier(classOf[P], classOf[P].getSimpleName)

  override def createPlugin(initializer: PluginInitializer): P = new PDepChain1(initializer, this)

  val dep = Helper.dependency(classOf[PDepChain0])
  val failedDep = Helper.dependency(classOf[PBadInstantiator])

  override val requiredDependencies: Set[Dependency[_ <: Plugin]] = Set(dep, failedDep)
}
