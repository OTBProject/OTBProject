package io.github.otbproject.otb.plugin.content

import java.util.Objects
import java.util.function.Function

import io.github.otbproject.otb.misc.{JLambda, ThreadUtil}

import scala.concurrent.{ExecutionContext, Future}

sealed class DataFactory[T, P <: PluginData] private[content](plugin: ContentPlugin,
                                                              initializer: T => P,
                                                              pClass: Class[P]) {
  private val identifier = new PluginDataTypeIdentifier(plugin, pClass)

  private[content] def provideData(initData: T, builder: PluginDataMap.Builder): Unit = {
    val f = Future { Objects.requireNonNull(initializer(initData)) }(DataFactory.executor)
    builder.put(identifier, f)
  }

  private[content] def getData(holder: PluginDataHolder[T]): P = holder.getPluginData.get(identifier)
}

object DataFactory {
  private[content] val executor: ExecutionContext =
    ExecutionContext.fromExecutorService(ThreadUtil.newCachedThreadPool("Plugin Data Provider"))

  /**
    * Returns a DataFactory which does nothing, and returns an empty PluginData when
    * [[DataFactory.getData()]] is called.
    *
    * @tparam T any type
    * @return a DataFactory which has essentially no effect
    */
  def empty[T]: DataFactory[T, PluginData] = {
    // Because both methods in EmptyDataFactory ignore the parameters
    // passed to them, this is safe for any value of T
    EmptyFactory.asInstanceOf[DataFactory[T, PluginData]]
  }

  /**
    *
    * @param plugin
    * @param initializer
    * @param pClass
    * @tparam T
    * @tparam P
    * @throws NullPointerException if any of the parameters are null
    * @return
    */
  @throws[NullPointerException]
  def of[T, P <: PluginData](plugin: ContentPlugin,
                             initializer: Function[T, P],
                             pClass: Class[P]): DataFactory[T, P] = {
    Objects.requireNonNull(initializer)
    of(plugin, JLambda.toFunction(initializer), pClass)
  }

  /**
    *
    *
    * @param plugin      something which extends ContentPlugin. Ignore the horrible looking
    *                    type parameter - it's necessary, but long and confusing to look at.
    * @param initializer a function which takes an object of type T and uses it to
    *                    generate some PluginData of type P
    * @param pClass      the class of the object returned by the initializer
    * @tparam T the type from which to generate some PluginData
    * @tparam P the (sub)type of the PluginData returned by the initializer
    * @throws NullPointerException if any of the parameters are null
    * @return a DataFactory which can be used to generate some P data based on a T, or
    *         retrieve a P from a [[PluginDataHolder]]
    */
  @throws[NullPointerException]
  def of[T, P <: PluginData](plugin: ContentPlugin,
                             initializer: T => P,
                             pClass: Class[P]): DataFactory[T, P] = {
    Objects.requireNonNull(plugin)
    Objects.requireNonNull(initializer)
    Objects.requireNonNull(pClass)
    new DataFactory(plugin, initializer, pClass)
  }

  private object EmptyFactory extends DataFactory[Any, PluginData](null, null, null) {
    override def provideData(initData: Any, builder: PluginDataMap.Builder) {}

    override def getData(holder: PluginDataHolder[Any]) = EmptyPluginData
  }

}
