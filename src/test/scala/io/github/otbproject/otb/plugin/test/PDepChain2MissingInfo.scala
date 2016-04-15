package io.github.otbproject.otb.plugin.test

import io.github.otbproject.otb.plugin.base.{Dependency, Plugin, PluginIdentifier, PluginInitializer}

class PDepChain2MissingInfo extends TestPluginInfo {
  override type P = PDepChain2Missing
  override val identifier: PluginIdentifier[P] = PluginIdentifier(classOf[P], classOf[P].getSimpleName)

  override def createPlugin(initializer: PluginInitializer): P = new PDepChain2Missing(initializer, this)

  val dep = Helper.dependency(classOf[PDepChain1])
  val brokenDep = Helper.dependency(classOf[PMissing])

  override val requiredDependencies: Set[Dependency[_ <: Plugin]] = Set(dep, brokenDep)
}
