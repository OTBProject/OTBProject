package io.github.otbproject.otb.plugin.test

import io.github.otbproject.otb.plugin.base.{Plugin, PluginInitializer}

class PDepChain3(initializer: PluginInitializer, pInfo: PDepChain3Info) extends Plugin(initializer) {
  override type Info = PDepChain3Info
  override val info: Info = pInfo
}
