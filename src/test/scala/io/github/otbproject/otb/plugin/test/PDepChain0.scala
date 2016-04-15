package io.github.otbproject.otb.plugin.test

import io.github.otbproject.otb.plugin.base.{Plugin, PluginInitializer}

class PDepChain0(initializer: PluginInitializer, pInfo: PDepChain0Info) extends Plugin(initializer) {
  override type Info = PDepChain0Info
  override val info: Info = pInfo
}
