package io.github.otbproject.otb.core.message.in.filter

import io.github.otbproject.otb.core.message.in.MessageIn
import io.github.otbproject.otb.misc.ThreadUtil

import scala.concurrent.{ExecutionContext, Future, Promise}

private[otb] object FilterProcessor {
  implicit private val executor =
    ExecutionContext.fromExecutorService(ThreadUtil.newCachedThreadPool("Filter Processor %d"))

  def apply(message: MessageIn, filters: List[MessageFilter]): Future[Unit] =
    processMessageWithFilters(message, filters)

  def processMessageWithFilters(message: MessageIn, filters: List[MessageFilter]): Future[Unit] = {
    val mainPromise = Promise[Unit]
    val resultPromise = Promise[FilterResult]

    val futures = filters.map(filter =>
      Future[Option[FilterResult]] {
        try {
          val option = filter(message)
          if (option.isDefined) mainPromise tryFailure new FilteredMessageException(resultPromise)
          option
        } catch {
          case _: Throwable =>
            // TODO: Log
            Option.empty
        }
      }
    )

    // Get the strictest FilterResult out of the Futures
    val eventualResult = Future.fold[Option[FilterResult], FilterResult](futures)(FilterResult.None) {
      (r, option) => if (option.isDefined) FilterResult.ordering.max(r, option.get) else r
    }
    resultPromise.completeWith(eventualResult)

    // When it is finished finding the strictest FilterResult, try to
    // complete mainPromise with success - if any of the filters produced
    // a result, this will have no effect because the promise was already
    // failed.
    eventualResult onSuccess { case _ => mainPromise trySuccess Unit }

    mainPromise.future
  }
}
