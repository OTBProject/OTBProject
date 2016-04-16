package io.github.otbproject.otb.plugin.test

import io.github.otbproject.otb.plugin.base._

class PCircularDep0Info extends TestPluginInfo {
  override type P = PCircularDep0
  override val identifier: PluginIdentifier[P] = PluginIdentifier(classOf[P], classOf[P].getSimpleName)

  override def createPlugin(initializer: PluginInitializer): PCircularDep0 = new PCircularDep0(initializer, this)

  val dep = Helper.dependency(classOf[PCircularDep1])

  override val requiredDependencies: Set[Dependency[_ <: Plugin]] = Set(dep)
}
