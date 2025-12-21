package com.ponkotuy.config

import com.ponkotuy.config.RichConfig
import com.typesafe.config.Config

case class DBConfig(url: String, username: String, password: Option[String])

object DBConfig {
  def load(conf: Config): Option[DBConfig] = {
    for {
      url <- conf.getOptString("url")
      username <- conf.getOptString("username")
      password = conf.getOptString("password")
    } yield DBConfig(url, username, password)
  }
}
