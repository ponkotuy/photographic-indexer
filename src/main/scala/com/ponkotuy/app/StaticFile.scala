package com.ponkotuy.app


import com.ponkotuy.app.CORSSetting
import com.ponkotuy.config.MyConfig
import com.ponkotuy.util.Extensions
import org.scalatra.*

import java.nio.file.Paths

class StaticFile(conf: MyConfig) extends ScalatraServlet with CORSSetting {
  get("/*") {
    val path = multiParams("splat").head
    contentType = Extensions.contentType(path)
    conf.app.photosDir.resolve(path).toFile
  }
}
