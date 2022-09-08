package com.ponkotuy.batch

import scala.concurrent.duration.Duration
import scala.util.control.NonFatal

class CronRunner(runner: Runnable, duration: Duration) extends Runnable {
  override def run(): Unit = {
    while(true) {
      try { runner.run() } catch {
        case NonFatal(e) =>
          e.printStackTrace()
      }
      Thread.sleep(duration.toMillis)
    }
  }
}

object CronRunner {
  def execute(runner: Runnable, duration: Duration): Unit = {
    new Thread(new CronRunner(runner, duration)).start()
  }
}
