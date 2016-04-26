package io.github.otbproject.otb.plugin.load

import io.github.otbproject.otb.plugin.base.{Plugin, PluginIdentifier}
import org.jgrapht.DirectedGraph
import org.jgrapht.graph.DefaultEdge

final case class LoadedPlugins(dependencyOrderedList: List[_ <: Plugin],
                               identifierMap: Map[PluginIdentifier[_ <: Plugin], _ <: Plugin],
                               private[otb] val dependencyGraph: DirectedGraph[PluginIdentifier[_ <: Plugin], DefaultEdge])
