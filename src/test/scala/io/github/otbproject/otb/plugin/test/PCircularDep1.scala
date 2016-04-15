package io.github.otbproject.otb.plugin.test

import io.github.otbproject.otb.plugin.base.{Plugin, PluginInitializer}

class PCircularDep1(initializer: PluginInitializer, pluginInfo3: PCircularDep1Info) extends Plugin(initializer) {
  override type Info = PCircularDep1Info
  override val info: Info = pluginInfo3
}
