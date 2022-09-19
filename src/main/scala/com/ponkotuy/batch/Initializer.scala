package com.ponkotuy.batch

import com.ponkotuy.config.{DBConfig, MyConfig}
import scalikejdbc.{ConnectionPool, ConnectionPoolSettings}

import scala.concurrent.duration.*

object Initializer {
  def run(conf: MyConfig): Unit = {
    Initializer.initDB(conf.db)
    val indexer = new Indexer(conf)
    CronRunner.execute(indexer, 1.hour)
  }

  def initDB(conf: DBConfig): Unit = {
    val settings = ConnectionPoolSettings(validationQuery = "select 1")
    ConnectionPool.singleton(conf.url, conf.username, conf.password.getOrElse(""), settings)
  }
}
