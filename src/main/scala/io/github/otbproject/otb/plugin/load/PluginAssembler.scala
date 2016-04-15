package io.github.otbproject.otb.plugin.load

import java.util.Objects
import java.util.stream.Collectors

import com.google.common.collect.HashMultimap
import io.github.otbproject.otb.core.Core
import io.github.otbproject.otb.misc.SLambda
import io.github.otbproject.otb.plugin.base._
import io.github.otbproject.otb.plugin.load.PluginAssembler._
import org.jgrapht.DirectedGraph
import org.jgrapht.alg.CycleDetector
import org.jgrapht.graph.{DefaultEdge, SimpleDirectedGraph}
import org.jgrapht.traverse.TopologicalOrderIterator

import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer
import scala.collection.{JavaConversions, mutable}

private final class PluginAssembler private(immutableSet: Set[PluginInfo]) {
  private val logger = Core.core.logger
  private val instantiatedPlugins: mutable.Map[PluginIdentifier[_], Plugin] = mutable.Map()
  private val infoSet = (mutable.Set.newBuilder ++= immutableSet).result

  private def assemble(): List[_ <: Plugin] = {
    logger.info("Checking plugins")

    // Filter out PluginInfo with null identifiers
    infoSet.retain(validInfo)
    if (infoSet.size < immutableSet.size) {
      logger.error("Skipping " + (immutableSet.size - infoSet.size) + " plugin(s) with null fields in PluginInfo")
    }

    removeDuplicates()
    pruneIfMissingDependencies()
    assemblePlugins()
  }

  /**
    * Remove plugins if name or class is used more than once
    */
  private def removeDuplicates(): Unit = {
    val nameMultimap = HashMultimap.create[String, PluginInfo]
    val classMultimap = HashMultimap.create[Class[_], PluginInfo]

    // Use multimap to count occurrences of each plugin name and class
    for (info <- infoSet) {
      nameMultimap.put(info.identifier.name, info)
      classMultimap.put(info.identifier.pClass, info)
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

  @tailrec
  private def pruneIfMissingDependencies(): Unit = {
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
      logger.error("Skipping plugins with missing dependencies: " + missingDependencies.map(_.identifier.toString))
      pruneIfMissingDependencies()
    } else removeWithCyclicDependencies()
  }

  private def removeWithCyclicDependencies(): Unit = {
    val graph = dependencyGraph()

    val cycleDetector = new CycleDetector(graph)
    val cyclicVertices = cycleDetector.findCycles()

    if (!cyclicVertices.isEmpty) {
      logger.error("Skipping plugins with circular dependencies: " + cyclicVertices)
      infoSet.retain(info => !cyclicVertices.contains(info.identifier))
      pruneIfMissingDependencies()
    }
  }

  private def dependencyGraph(): DirectedGraph[PluginIdentifier[_], DefaultEdge] = {
    val identifierMap = mapIdentifiers()
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

  private def mapIdentifiers(): Map[PluginIdentifier[_], PluginInfo] = {
    infoSet.map(e => (e.identifier, e)).toMap
  }

  private def assemblePlugins(): List[Plugin] = {
    val identifierMap = mapIdentifiers()
    val plugins = ListBuffer[Plugin]()

    logger.info("Building dependency tree")

    val graph = dependencyGraph()
    val iterator = new TopologicalOrderIterator(graph)

    var failed = false
    for (identifier <- JavaConversions.asScalaIterator(iterator)
         if !failed) {
      if (instantiatedPlugins contains identifier) {
        plugins += instantiatedPlugins.get(identifier).get
      } else {
        try {
          val info = identifierMap.get(identifier).get
          logger.debug("Instantiating plugin: " + identifier)
          val p = validatePlugin(info.createPlugin(getPluginInitializer(info)), info)
          plugins += p
          instantiatedPlugins.put(identifier, p)
        } catch {
          case e: Throwable =>
            logger.error("Error instantiating plugin: " + identifier)
            logger.catching(e)
            infoSet.remove(identifierMap.get(identifier).get)
            failed = true
        }
      }
    }

    if (failed) {
      reassemblePlugins()
    } else {
      logger.info("Finished building plugins")
      plugins.toList
    }
  }

  private def getPluginInitializer(info: PluginInfo): PluginInitializer = {
    val map: Map[Dependency[_ <: Plugin], Plugin] =
      info.requiredDependencies.toStream.map(dep => (dep, instantiatedPlugins.get(dep.identifier).get)).toMap
    // TODO: implement properly (logger)
    new PluginInitializer(Core.core.logger, Core.core.eventBus, map)
  }

  private def reassemblePlugins(): List[_ <: Plugin] = {
    pruneIfMissingDependencies()
    assemblePlugins()
  }
}

private[load] object PluginAssembler {
  def assembleFrom(infoSet: Set[PluginInfo]): List[_ <: Plugin] = {
    new PluginAssembler(infoSet).assemble()
  }

  private def validInfo(info: PluginInfo): Boolean = {
    info.identifier != null &&
      info.requiredDependencies != null &&
      info.version != null
  }

  @throws[NullPointerException]
  @throws[IllegalStateException]
  private def validatePlugin(plugin: Plugin, info: PluginInfo): Plugin = {
    Objects.requireNonNull(plugin, "Plugin cannot be null")
    if (plugin.info != info) {
      throw new IllegalStateException("Plugin's info does not match PluginInfo which created it")
    }
    plugin
  }
}
