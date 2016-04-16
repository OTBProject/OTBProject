package io.github.otbproject.otb.plugin.test

import io.github.otbproject.otb.plugin.base._

class PCircularDep1Info extends TestPluginInfo {
  override type P = PCircularDep1
  override val identifier: PluginIdentifier[P] = PluginIdentifier(classOf[P], classOf[P].getSimpleName)

  override def createPlugin(initializer: PluginInitializer): PCircularDep1 = new PCircularDep1(initializer, this)

  val dep = Helper.dependency(classOf[PCircularDep0])

  override val requiredDependencies: Set[Dependency[_ <: Plugin]] = Set(dep)
}
