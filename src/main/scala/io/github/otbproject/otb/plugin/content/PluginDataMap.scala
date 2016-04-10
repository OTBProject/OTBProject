package io.github.otbproject.otb.plugin.content

import java.util.concurrent.TimeUnit

import scala.collection.mutable
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future, TimeoutException}

private[content] final class PluginDataMap private(map: Map[PluginDataTypeIdentifier[_], Future[_ <: PluginData]]) {
  private val dataMap: Map[PluginDataTypeIdentifier[_], Future[_ <: PluginData]] = map

  @throws[DataRetrievalException]
  def get[T <: PluginData](identifier: PluginDataTypeIdentifier[T]): T = {
    try {
      val future = dataMap.get(identifier).get
      val data = Await.result(future, PluginDataMap.TIMEOUT_DURATION)
      // Cast never fails because it is constrained that a type T object only gets
      // inserted with a PluginDataTypeIdentifier[T] as its key
      identifier.tClass.cast(data)
    } catch {
      case e: TimeoutException => throw new DataRetrievalException(identifier.plugin)
      case e: NoSuchElementException => throw new DataRetrievalException("No data found for provided plugin", e)
      case e: Throwable => throw new DataRetrievalException(e)
    }
  }
}

private[content] object PluginDataMap {
  val TIMEOUT_DURATION = Duration(4, TimeUnit.SECONDS)

  def newBuilder: Builder = new Builder

  final class Builder {
    private val map = new mutable.HashMap[PluginDataTypeIdentifier[_], Future[_ <: PluginData]]

    // Exception should never be thrown
    @throws[IllegalArgumentException]
    def put[T <: PluginData](identifier: PluginDataTypeIdentifier[T], dataFuture: Future[T]) = {
      if (map contains identifier) {
        throw new IllegalArgumentException("Plugin-Class mapping already present: " + identifier)
      }
      map.put(identifier, dataFuture)
    }

    def build: PluginDataMap = new PluginDataMap(map.toMap)
  }

}


