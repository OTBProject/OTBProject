package io.github.otbproject.otb.plugin.test

import io.github.otbproject.otb.plugin.base.{Dependency, Plugin, PluginIdentifier, PluginInitializer}

class PDepChain3Info extends TestPluginInfo {
  override type P = PDepChain3
  override val identifier: PluginIdentifier[P] = PluginIdentifier(classOf[P], classOf[P].getSimpleName)

  override def createPlugin(initializer: PluginInitializer): P = new PDepChain3(initializer, this)

  val dep = Helper.dependency(classOf[PDepChain2Missing])

  override val requiredDependencies: Set[Dependency[_ <: Plugin]] = Set(dep)
}
