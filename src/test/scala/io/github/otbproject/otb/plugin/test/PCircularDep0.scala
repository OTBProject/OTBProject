package io.github.otbproject.otb.plugin.test

import io.github.otbproject.otb.plugin.base.{Plugin, PluginInitializer}

class PCircularDep0(initializer: PluginInitializer, pluginInfo2: PCircularDep0Info) extends Plugin(initializer) {
  override type Info = PCircularDep0Info
  override val info: Info = pluginInfo2
}
