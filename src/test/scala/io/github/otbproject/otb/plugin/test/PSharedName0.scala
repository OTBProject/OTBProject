package io.github.otbproject.otb.plugin.test

import io.github.otbproject.otb.plugin.base.{Plugin, PluginInitializer}

class PSharedName0(initializer: PluginInitializer, pInfo: PSharedName0Info) extends Plugin(initializer) {
  override type Info = PSharedName0Info
  override val info: Info = pInfo
}
