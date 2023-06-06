package com.ponkotuy.config

import com.typesafe.config.Config

import java.net.URI

case class ClipConfig(url: String) {
  lazy val uri: URI = URI.create(url)
}

object ClipConfig {
  def load(conf: Config): Option[ClipConfig] = {
    for {
      url <- conf.getOptString("url")
    } yield ClipConfig(url)
  }
}
