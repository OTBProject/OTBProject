package io.github.otbproject.otb.plugin.content

import scala.collection.mutable

final class PluginDataMap private(map: Map[PluginDataTypeIdentifier[_], _ <: PluginData]) {
    private val dataMap: Map[PluginDataTypeIdentifier[_], _ <: PluginData] = map

    private[plugin] def get[T <: PluginData](plugin: ContentPlugin, tClass: Class[T]): Option[T] = {
        Option(tClass.cast(dataMap.get(PluginDataTypeIdentifier(plugin, tClass))))
    }
}

object PluginDataMap {
    def newBuilder: Builder = new Builder

    final class Builder {
        val mutableMap = new mutable.HashMap[PluginDataTypeIdentifier[_], PluginData]()

        @throws[IllegalArgumentException]
        def put[T <: PluginData](plugin: ContentPlugin, tClass: Class[T], data: T) = {
            val identifier: PluginDataTypeIdentifier[T] = PluginDataTypeIdentifier(plugin, tClass)
            if (mutableMap contains identifier) {
                throw new IllegalArgumentException("Plugin-Class mapping already present: " + identifier)
            }
            mutableMap.put(identifier, data)
        }

        def build: PluginDataMap = new PluginDataMap(mutableMap.toMap)
    }

}
