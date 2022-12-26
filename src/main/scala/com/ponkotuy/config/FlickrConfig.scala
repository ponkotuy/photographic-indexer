package com.ponkotuy.config

import com.ponkotuy.flickr.NSID
import com.typesafe.config.Config

case class FlickrConfig(key: String, secret: String, me: NSID)

object FlickrConfig {
  def load(conf: Config): Option[FlickrConfig] = {
    for {
      key <- conf.getOptString("key")
      secret <- conf.getOptString("secret")
      me <- conf.getOptString("me")
    } yield FlickrConfig(key, secret, NSID(me))
  }
}
