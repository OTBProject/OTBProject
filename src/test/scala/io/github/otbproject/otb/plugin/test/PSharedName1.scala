package io.github.otbproject.otb.plugin.test

import io.github.otbproject.otb.plugin.base.{Plugin, PluginInitializer}

class PSharedName1(initializer: PluginInitializer, pInfo: PSharedName1Info) extends Plugin(initializer) {
  override type Info = PSharedName1Info
  override val info: Info = pInfo
}
