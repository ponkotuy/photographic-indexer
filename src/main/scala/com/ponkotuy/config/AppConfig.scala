package com.ponkotuy.config

import com.typesafe.config.Config

import java.nio.file.{ Path, Paths }

case class AppConfig(photosDir: Path, email: Option[String])

object AppConfig {
  def load(conf: Config): Option[AppConfig] = {
    for {
      photosDir <- conf.getOptString("photos_dir")
      email = conf.getOptString("email")
    } yield AppConfig(Paths.get(photosDir), email)
  }
}
