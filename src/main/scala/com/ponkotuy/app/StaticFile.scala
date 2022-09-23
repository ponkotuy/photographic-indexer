package com.ponkotuy.app


import com.ponkotuy.app.CORSSetting
import com.ponkotuy.config.MyConfig
import com.ponkotuy.util.Extensions
import org.scalatra.*
import org.scalatra.servlet.{FileUploadSupport, MultipartConfig, SizeConstraintExceededException}

import java.nio.file.{Files, OpenOption, Paths, StandardOpenOption}

class StaticFile(conf: MyConfig) extends ScalatraServlet with MyUploadSupport with CORSSetting {
  get("/*") {
    val path = multiParams("splat").head
    contentType = Extensions.contentType(path)
    conf.app.photosDir.resolve(path).toFile
  }

  post("/*") {
    val path = multiParams("splat").head
    val content = fileParams("file").get()
    Files.write(conf.app.photosDir.resolve(path), content)
  }

  put("/*") {
    val path = multiParams("splat").head
    val content = fileParams("file").get()
    Files.write(conf.app.photosDir.resolve(path), content, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)
  }
  error{
    case _: SizeConstraintExceededException => RequestEntityTooLarge("too much!")
  }
}
