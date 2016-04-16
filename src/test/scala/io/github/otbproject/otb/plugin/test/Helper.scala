package io.github.otbproject.otb.plugin.test

import io.github.otbproject.otb.misc.CoreVersion
import io.github.otbproject.otb.plugin.base.{Dependency, Plugin, PluginIdentifier, PluginInfo}

object Helper {
  val infoSet: Set[PluginInfo] = Set(
    new PSimpleValidInfo,
    new PWrongDepNameInfo,
    new PCircularDep0Info,
    new PCircularDep1Info,
    new PDepChain0Info,
    new PDepChain1Info,
    new PDepChain2MissingInfo,
    new PDepChain3Info,
    new PBadInstantiatorInfo,
    new PSharedName0Info,
    new PSharedName1Info,
    new PSharedClassInfo0,
    new PSharedClassInfo1)

  private[plugin] def identifier[P <: Plugin](pClass: Class[P]): PluginIdentifier[P] =
    PluginIdentifier(pClass, pClass.getSimpleName)

  private[test] def dependency[P <: Plugin](pClass: Class[P]): Dependency[P] =
    new Dependency(identifier(pClass), CoreVersion(1, 0, 0))
}
