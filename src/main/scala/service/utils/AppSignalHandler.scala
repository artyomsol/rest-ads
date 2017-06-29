package service.utils

import java.util.concurrent.atomic.AtomicBoolean

import sun.misc.{Signal, SignalHandler}

/**
 * Project: rest-ads
 * Package: service.utils
 * Created by asoloviov on 6/29/17 10:31 PM.
 */
class AppSignalHandler extends SignalHandler {

  private val terminated = new AtomicBoolean(false)

  private val SIGINT = "INT"
  private val SIGTERM = "TERM"
  Signal.handle(new Signal(SIGINT), this)
  Signal.handle(new Signal(SIGTERM), this)

  class Hook(body: => Unit) {
    def run() = body
  }

  private var reapers: List[Hook] = List.empty[Hook]

  def registerReaper(callback: => Unit) = reapers = new Hook(callback) :: reapers

  override def handle(signal: Signal): Unit = {
    if (terminated.compareAndSet(false, true) && List(SIGINT, SIGTERM).contains(signal.getName)) {
      reapers.reverse.foreach(h => h.run())
      System.exit(0)
    }
  }
}

object AppSignalHandler {
  def apply() = new AppSignalHandler()
}
