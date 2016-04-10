package io.github.otbproject.otb.misc

import java.util.concurrent.{ExecutorService, Executors, ThreadFactory}

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
    Executors.newCachedThreadPool(newThreadFactory)
  }

  def newCachedThreadPool(nameFormat: String): ExecutorService = {
    Executors.newCachedThreadPool(newThreadFactory(nameFormat))
  }

  def interruptIfInterruptedException(e: Exception) {
    if (e.isInstanceOf[InterruptedException]) {
      Thread.currentThread.interrupt()
    }
  }
}
