package io.github.otbproject.otb.plugin.base

import io.github.otbproject.otb.misc.CoreVersion

/**
  *
  * @param identifier
  * @param minVersion minimum version which is acceptable
  * @param maxMajorVersion first major version which is not acceptable
  * @tparam P
  */
@throws[IllegalArgumentException]
final case class Dependency[P <: Plugin](identifier: PluginIdentifier[P],
                                         minVersion: CoreVersion,
                                         maxMajorVersion: Int) {
  // Make sure maxMajorVersion is valid
  if (maxMajorVersion < 0) {
    throw new IllegalArgumentException("Maximum major version must be non-negative.")
  }

  def this(identifier: PluginIdentifier[P], minVersion: CoreVersion) =
    this(identifier, minVersion, minVersion.major + 1)

  private[plugin] def satisfiedBy(info: PluginInfo): Boolean = {
    (identifier == info.identifier) &&
      (minVersion <= info.version) &&
      (info.version.major < maxMajorVersion)
  }
}
