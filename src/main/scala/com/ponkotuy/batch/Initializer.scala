package com.ponkotuy.batch

import com.ponkotuy.config.{ DBConfig, MyConfig }
import org.apache.commons.dbcp2.BasicDataSource
import scalikejdbc.{ ConnectionPool, DataSourceConnectionPool }

import scala.concurrent.duration.FiniteDuration

object Initializer {
  def run(conf: MyConfig): Unit = {
    import scala.concurrent.duration.*
    Initializer.initDB(conf.db)
    new ImageFileChecker(conf.app).run()
    val indexer = new Indexer(conf.app)
    val clip = conf.clip.map(new CLIPIndexer(_, conf.app))
    CronRunner.execute(indexer, 1.hour)
    clip.foreach(CronRunner.execute(_, 1.day))
    runAfter(10.minutes) {
      new ExifCacheBatch(conf.app).run()
    }
  }

  def initDB(conf: DBConfig): Unit = {
    import java.time.Duration
    println(s"Initialize DB: ${ conf.url }")
    val ds = new BasicDataSource
    ds.setUrl(conf.url)
    ds.setUsername(conf.username)
    conf.password.foreach(ds.setPassword)
    ds.setDurationBetweenEvictionRuns(Duration.ofSeconds(60))
    ds.setMinIdle(2)
    ds.setValidationQuery("SELECT 1")
    ds.setMaxWait(Duration.ofSeconds(5))
    ConnectionPool.singleton(new DataSourceConnectionPool(ds))
  }

  private def runAfter(delay: FiniteDuration)(body: => Unit): Thread = {
    val t = new Thread(() => {
      Thread.sleep(delay.toMillis)
      body
    })
    t.start()
    t
  }
}
