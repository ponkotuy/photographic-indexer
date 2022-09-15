package com.ponkotuy.batch

import com.ponkotuy.config.{DBConfig, MyConfig}
import scalikejdbc.ConnectionPool

import scala.concurrent.duration._

object Initializer {
  def run(): Unit = {
    val conf = MyConfig.load().getOrElse(throw new RuntimeException("ConfigError"))
    Initializer.initDB(conf.db)
//    val indexer = new Indexer(conf)
//    CronRunner.execute(indexer, 1.hour)
  }

  def initDB(conf: DBConfig): Unit = {
    ConnectionPool.singleton(conf.url, conf.username, conf.password.getOrElse(""))
  }
}
