package io.github.otbproject.otb.plugin.test

import io.github.otbproject.otb.plugin.base._

class PSimpleValidInfo extends TestPluginInfo {
  override type P = PSimpleValid
  override val identifier: PluginIdentifier[P] = PluginIdentifier(classOf[P], classOf[P].getSimpleName)

  override def createPlugin(initializer: PluginInitializer): PSimpleValid = new PSimpleValid(initializer, this)
  override val requiredDependencies: Set[Dependency[_ <: Plugin]] = Set()
}
