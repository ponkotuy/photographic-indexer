package com.ponkotuy.config

import com.typesafe.config.{Config, ConfigFactory}
import com.ponkotuy.config.RichConfig

import java.nio.file.{Path, Paths}

case class MyConfig(app: AppConfig, db: DBConfig, flickr: Option[FlickrConfig])

object MyConfig {
  def load(): Option[MyConfig] = {
    val conf = ConfigFactory.load()
    for {
      appRaw <- conf.getOptConfig("app")
      app <- AppConfig.load(appRaw)
      dbRaw <- conf.getOptConfig("db")
      db <- DBConfig.load(dbRaw)
    } yield {
      val flickrRaw = conf.getOptConfig("flickr")
      val flickr = flickrRaw.flatMap(FlickrConfig.load)
      MyConfig(app, db, flickr)
    }
  }
}
