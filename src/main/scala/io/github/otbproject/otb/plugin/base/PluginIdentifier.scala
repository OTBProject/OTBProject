package io.github.otbproject.otb.plugin.base

import java.util.Objects

final case class PluginIdentifier[P <: Plugin](pClass: Class[P], name: String) {
  Objects.requireNonNull(pClass)
  Objects.requireNonNull(name)

  override def toString: String = "(" + name + " : " + pClass.getName + ")"
}
