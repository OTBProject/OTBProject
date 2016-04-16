package io.github.otbproject.otb.plugin.test

import io.github.otbproject.otb.misc.CoreVersion
import io.github.otbproject.otb.plugin.base._

class PWrongDepNameInfo extends TestPluginInfo {
  override type P = PWrongDepName
  override val identifier: PluginIdentifier[P] = PluginIdentifier(classOf[P], classOf[P].getSimpleName)

  override def createPlugin(initializer: PluginInitializer): PWrongDepName = new PWrongDepName(initializer, this)

  val dep = new Dependency(PluginIdentifier(classOf[PSimpleValid], "wrong name"), CoreVersion(1, 0, 0))

  override val requiredDependencies: Set[Dependency[_ <: Plugin]] = Set(dep)
}
