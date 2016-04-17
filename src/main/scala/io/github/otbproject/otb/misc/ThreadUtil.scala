package io.github.otbproject.otb.misc

import java.util.concurrent._

import com.google.common.util.concurrent.ThreadFactoryBuilder

object ThreadUtil {
  // TODO: implement uncaught exception handler
  val UNCAUGHT_EXCEPTION_HANDLER = SLambda.toUncaughtExceptionHandler((t, e) => {
    //      App.logger.error("Thread crashed: " + t.getName())
    //      App.logger.catching(e)
    //      Watcher.logException()
  })

  Thread.setDefaultUncaughtExceptionHandler(UNCAUGHT_EXCEPTION_HANDLER)

  def newSingleThreadExecutor: ExecutorService = {
    Executors.newSingleThreadExecutor(newThreadFactory)
  }

  def newThreadFactory: ThreadFactory = {
    new ThreadFactoryBuilder()
      .setUncaughtExceptionHandler(UNCAUGHT_EXCEPTION_HANDLER)
      .build
  }

  def newSingleThreadExecutor(nameFormat: String): ExecutorService = {
    Executors.newSingleThreadExecutor(newThreadFactory(nameFormat))
  }

  def newThreadFactory(nameFormat: String): ThreadFactory = {
    new ThreadFactoryBuilder()
      .setNameFormat(nameFormat)
      .setUncaughtExceptionHandler(UNCAUGHT_EXCEPTION_HANDLER)
      .build
  }

  def newCachedThreadPool: ExecutorService = {
    newThreadPoolExecutor(newThreadFactory)
  }

  def newCachedThreadPool(nameFormat: String): ExecutorService = {
    newThreadPoolExecutor(newThreadFactory(nameFormat))
  }

  def interruptIfInterruptedException(e: Exception) {
    if (e.isInstanceOf[InterruptedException]) {
      Thread.currentThread.interrupt()
    }
  }

  private object ThreadPoolLimits {
    val minPoolSize = 8
    val maxPoolSize = 16
    val threadTimeout = 60L
    val maxQueueSize = 16
  }

  private def newThreadPoolExecutor(factory: ThreadFactory): ExecutorService = {
    val pool = new ThreadPoolExecutor(
      ThreadPoolLimits.minPoolSize,
      ThreadPoolLimits.maxPoolSize,
      ThreadPoolLimits.threadTimeout,
      TimeUnit.SECONDS,
      new LinkedBlockingQueue[Runnable](ThreadPoolLimits.maxQueueSize),
      factory)
    pool.allowCoreThreadTimeOut(true)
    pool
  }
}
