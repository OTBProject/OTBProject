package io.github.otbproject.otb.plugin.load

import java.util.Objects
import java.util.stream.Collectors

import com.google.common.collect.HashMultimap
import io.github.otbproject.otb.core.Core
import io.github.otbproject.otb.misc.SLambda
import io.github.otbproject.otb.plugin.base.{Plugin, PluginIdentifier, PluginInfo, PluginInitializer}
import org.jgrapht.DirectedGraph
import org.jgrapht.alg.CycleDetector
import org.jgrapht.graph.{DefaultEdge, SimpleDirectedGraph}
import org.jgrapht.traverse.TopologicalOrderIterator

import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer
import scala.collection.{JavaConversions, mutable}

private[load] final class PluginAssembler {
  private val logger = Core.core.logger

  def assembleFrom(infoSet: Set[PluginInfo]): List[_ <: Plugin] = {
    val mutableSet = (mutable.Set.newBuilder ++= infoSet).result

    logger.info("Checking plugins")

    // Filter out PluginInfo with null identifiers
    mutableSet.retain(_.identifier != null)
    if (mutableSet.size < infoSet.size) {
      logger.error("Skipping " + (infoSet.size - mutableSet.size) + " plugin(s) with null identifiers")
    }

    removeDuplicates(mutableSet)
    pruneIfMissingDependencies(mutableSet)
    assemblePlugins(mutableSet)
  }

  /**
    * Remove plugins if name or class is used more than once
    */
  private def removeDuplicates(infoSet: mutable.Set[PluginInfo]): Unit = {
    val nameMultimap = HashMultimap.create[String, PluginInfo]
    val classMultimap = HashMultimap.create[Class[_], PluginInfo]

    // Use multimap to count occurrences of each plugin name and class
    for (info <- infoSet) {
      nameMultimap.put(info.identifier.pluginName, info)
      classMultimap.put(info.identifier.pluginClass, info)
    }

    val infoToString = (info: PluginInfo) => "(" + info.getClass.getName + " -> " + info.identifier.toString + ")"

    // Remove duplicate names
    for (entry <- JavaConversions.asScalaSet(nameMultimap.asMap.entrySet)) {
      val set = entry.getValue
      if (set.size > 1) {
        logger.error("Duplicate plugin name '" + entry.getKey + "' detected - skipping "
          + set.size + " plugins with this name: "
          + set.stream.map[String](SLambda.toFunction(infoToString)).collect(Collectors.joining(", ", "[", "]")))
        infoSet.retain((info: PluginInfo) => !set.contains(info))
      }
    }

    // Remove duplicate classes
    for (entry <- JavaConversions.asScalaSet(classMultimap.asMap.entrySet)) {
      val set = entry.getValue
      if (set.size > 1) {
        logger.error("Duplicate plugin class '" + entry.getKey + "' detected - skipping "
          + set.size + " plugins with this class: "
          + set.stream.map[String](SLambda.toFunction(infoToString)).collect(Collectors.joining(", ", "[", "]")))
        infoSet.retain((info: PluginInfo) => !set.contains(info))
      }
    }
  }

  // Maybe works?
  @tailrec
  private def pruneIfMissingDependencies(infoSet: mutable.Set[PluginInfo]): Unit = {
    val missingDependencies = mutable.Set[PluginInfo]()

    // Does stuff
    for (info <- infoSet) {
      var allSatisfied = true
      for (dep <- info.requiredDependencies
           if allSatisfied /* Short circuit */ ) {
        var satisfied = false
        for (other <- infoSet
             if !satisfied // short circuit
             if dep satisfiedBy other) {
          satisfied = true
        }
        if (!satisfied) allSatisfied = false
      }
      if (!allSatisfied) missingDependencies.add(info)
    }

    if (missingDependencies.nonEmpty) {
      infoSet.retain(!missingDependencies.contains(_))
      logger.error("Skipped plugins missing dependencies: " + missingDependencies.map(_.identifier.toString))
      pruneIfMissingDependencies(infoSet)
    } else removeWithCyclicDependencies(infoSet)
  }

  private def removeWithCyclicDependencies(infoSet: mutable.Set[PluginInfo]): Unit = {
    val graph = dependencyGraph(infoSet)

    val cycleDetector = new CycleDetector(graph)
    val cyclicVertices = cycleDetector.findCycles()

    if (!cyclicVertices.isEmpty) {
      logger.error("Skipping plugins with circular dependencies: " + cyclicVertices)
      infoSet.retain(info => !cyclicVertices.contains(info.identifier))
      pruneIfMissingDependencies(infoSet)
    }
  }

  private def dependencyGraph(infoSet: mutable.Set[PluginInfo]): DirectedGraph[PluginIdentifier[_], DefaultEdge] = {
    val identifierMap = mapIdentifiers(infoSet)
    val graph = new SimpleDirectedGraph[PluginIdentifier[_], DefaultEdge](classOf[DefaultEdge])

    // Add PluginInfo as vertices
    infoSet.foreach(info => graph.addVertex(info.identifier))

    // Create dependency edges
    for (vertex <- JavaConversions.asScalaSet(graph.vertexSet())) {
      val info = identifierMap.get(vertex).get
      for (dep <- info.requiredDependencies) {
        graph.addEdge(dep.identifier, vertex)
      }
    }
    graph
  }

  private def mapIdentifiers(infoSet: mutable.Set[PluginInfo]): Map[PluginIdentifier[_], PluginInfo] = {
    infoSet.map(e => (e.identifier, e)).toMap
  }

  private def assemblePlugins(infoSet: mutable.Set[PluginInfo]): List[Plugin] = {
    val identifierMap = mapIdentifiers(infoSet)
    val plugins = ListBuffer[Plugin]()

    logger.info("Building dependency tree")

    val graph = dependencyGraph(infoSet)
    val topologicalOrderIterator = new TopologicalOrderIterator(graph)

    var failed = false
    for (identifier <- JavaConversions.asScalaIterator(topologicalOrderIterator)
         if !failed) {
      try {
        val info = identifierMap.get(identifier).get
        plugins += Objects.requireNonNull(info.createPlugin(getPluginInitializer(info)))
      } catch {
        case e: Throwable =>
          logger.error("Error instantiating plugin: " + identifier)
          logger.catching(e)
          infoSet.remove(identifierMap.get(identifier).get)
          failed = true
      }
    }

    if (failed) reassemblePlugins(infoSet)
    else plugins.toList
  }

  private def getPluginInitializer(pluginInfo: PluginInfo): PluginInitializer = {
    // TODO: implement properly
    new PluginInitializer(Core.core.logger, Core.core.eventBus)
  }

  private def reassemblePlugins(infoSet: mutable.Set[PluginInfo]): List[_ <: Plugin] = {
    pruneIfMissingDependencies(infoSet)
    assemblePlugins(infoSet)
  }
}
