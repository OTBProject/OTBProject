package io.github.otbproject.otb.plugin.test

import io.github.otbproject.otb.misc.CoreVersion
import io.github.otbproject.otb.plugin.base.{PluginIdentifier, PluginInfo}

abstract class TestPluginInfo extends PluginInfo {
  override val version = CoreVersion(1, 0, 0)
}
