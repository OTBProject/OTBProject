package io.github.otbproject.otb.plugin.test

import io.github.otbproject.otb.plugin.base.{Plugin, PluginInitializer}

class PSharedClass(initializer: PluginInitializer, pInfo: TestPluginInfo) extends Plugin(initializer) {
  override type Info = TestPluginInfo
  override val info: Info = pInfo
}
