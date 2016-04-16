package io.github.otbproject.otb.plugin.test

import io.github.otbproject.otb.plugin.base.{Plugin, PluginInitializer}

class PBadInstantiator(initializer: PluginInitializer, pInfo: PBadInstantiatorInfo) extends Plugin(initializer) {
  override type Info = PBadInstantiatorInfo
  override val info: Info = pInfo

  throw new RuntimeException("Failed initialization")
}
