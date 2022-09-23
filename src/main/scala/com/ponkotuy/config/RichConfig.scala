package com.ponkotuy.config

import com.typesafe.config.Config

implicit class RichConfig(val underlying: Config) extends AnyVal {
  def getOptBoolean(path: String): Option[Boolean] = if(underlying.hasPath(path)) {
    Some(underlying.getBoolean(path))
  } else {
    None
  }

  def getOptString(path: String): Option[String] = if(underlying.hasPath(path)) {
    Some(underlying.getString(path))
  } else {
    None
  }

  def getOptInt(path: String): Option[Int] = if(underlying.hasPath(path)) {
    Some(underlying.getInt(path))
  } else {
    None
  }

  def getOptConfig(path: String): Option[Config] = if(underlying.hasPath(path)) {
    Some(underlying.getConfig(path))
  } else {
    None
  }
}
