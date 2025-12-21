package com.ponkotuy.config

import com.typesafe.config.{ Config, ConfigFactory }
import com.ponkotuy.config.RichConfig

import java.nio.file.{ Path, Paths }

case class MyConfig(app: AppConfig, db: DBConfig, flickr: Option[FlickrConfig], clip: Option[ClipConfig])

object MyConfig {
  def load(): Option[MyConfig] = {
    val conf = ConfigFactory.load()
    for {
      appRaw <- conf.getOptConfig("app")
      app <- AppConfig.load(appRaw)
      dbRaw <- conf.getOptConfig("db")
      db <- DBConfig.load(dbRaw)
      flickrRaw = conf.getOptConfig("flickr")
      flickr = flickrRaw.flatMap(FlickrConfig.load)
      clipRaw = conf.getOptConfig("clip")
      clip = clipRaw.flatMap(ClipConfig.load)
    } yield {
      MyConfig(app, db, flickr, clip)
    }
  }
}
