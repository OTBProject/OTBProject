package io.github.otbproject.otb.plugin.test

import io.github.otbproject.otb.plugin.base.{Plugin, PluginInitializer}

class PWrongDepName(initializer: PluginInitializer, pluginInfo1: PWrongDepNameInfo) extends Plugin(initializer) {
  override type Info = PWrongDepNameInfo
  override val info: Info = pluginInfo1
}
