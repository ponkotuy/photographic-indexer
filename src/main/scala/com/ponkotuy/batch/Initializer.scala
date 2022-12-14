package com.ponkotuy.batch

import com.ponkotuy.config.{DBConfig, FlickrConfig, MyConfig}
import com.ponkotuy.flickr.{FlickrAccessor, NSID}
import org.apache.commons.dbcp2.BasicDataSource
import scalikejdbc.{ConnectionPool, ConnectionPoolSettings, DataSourceConnectionPool}

import scala.concurrent.duration.*

object Initializer {
  def run(conf: MyConfig): Unit = {
    Initializer.initDB(conf.db)
    val indexer = new Indexer(conf)
    CronRunner.execute(indexer, 1.hour)
  }

  def initDB(conf: DBConfig): Unit = {
    println(s"Initialize DB: ${conf.url}")
    val ds = new BasicDataSource
    ds.setUrl(conf.url)
    ds.setUsername(conf.username)
    conf.password.foreach(ds.setPassword)
    ds.setTimeBetweenEvictionRunsMillis(60.seconds.toMillis)
    ds.setMinIdle(2)
    ds.setValidationQuery("SELECT 1")
    ds.setMaxWaitMillis(5.seconds.toMillis)
    ConnectionPool.singleton(new DataSourceConnectionPool(ds))
  }
}
