package io.github.otbproject.otb.plugin.base

import java.util.Objects

final case class PluginIdentifier[P <: Plugin](pluginClass: Class[P], pluginName: String) {
  Objects.requireNonNull(pluginClass)
  Objects.requireNonNull(pluginName)

  override def toString: String = "(" + pluginName + " : " + pluginClass.getName + ")"
}
