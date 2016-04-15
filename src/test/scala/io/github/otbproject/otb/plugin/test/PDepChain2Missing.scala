package io.github.otbproject.otb.plugin.test

import io.github.otbproject.otb.plugin.base.{Plugin, PluginInitializer}

class PDepChain2Missing(initializer: PluginInitializer, pInfo: PDepChain2MissingInfo) extends Plugin(initializer) {
  override type Info = PDepChain2MissingInfo
  override val info: Info = pInfo
}
