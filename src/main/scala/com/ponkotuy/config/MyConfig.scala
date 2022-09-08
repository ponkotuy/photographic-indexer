package com.ponkotuy.config

import com.typesafe.config.{Config, ConfigFactory}
import com.ponkotuy.config.RichConfig

import java.nio.file.{Path, Paths}

case class MyConfig(app: AppConfig, db: DBConfig)

object MyConfig {
  def load(): Option[MyConfig] = {
    val conf = ConfigFactory.load()
    for {
      appRaw <- conf.getOptConfig("app")
      app <- AppConfig.load(appRaw)
      dbRaw <- conf.getOptConfig("db")
      db <- DBConfig.load(dbRaw)
    } yield MyConfig(app, db)
  }
}
