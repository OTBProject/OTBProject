package io.github.otbproject.otb.plugin.test

import io.github.otbproject.otb.plugin.base.{Plugin, PluginInitializer}

class PMissing(initializer: PluginInitializer, pInfo: PSimpleValidInfo) extends Plugin(initializer) {
  override type Info = PSimpleValidInfo
  override val info: Info = pInfo
}
