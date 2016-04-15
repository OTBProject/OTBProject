package io.github.otbproject.otb.plugin.base

class InvalidDependencyException(identifier: PluginIdentifier[_])
  extends IllegalArgumentException(
    "Attempted to retrieve a dependency which was not registered as required: " + identifier) {

}
