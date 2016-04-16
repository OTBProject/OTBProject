package io.github.otbproject.otb.plugin.test

import io.github.otbproject.otb.plugin.base.{Plugin, PluginInitializer}

class PDepChain1(initializer: PluginInitializer, pInfo: PDepChain1Info) extends Plugin(initializer) {
  override type Info = PDepChain1Info
  override val info: Info = pInfo
}
