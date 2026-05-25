package com.ponkotuy.app

import com.ponkotuy.config.AppConfig
import com.ponkotuy.util.Extensions
import org.scalatra.*
import org.scalatra.servlet.SizeConstraintExceededException

import java.nio.file.{ Files, OpenOption, StandardOpenOption }

class PrivateStaticFile(appConf: AppConfig) extends ScalatraServlet with MyUploadSupport with CORSSetting {
  get("/*") {
    val path = multiParams("splat").head
    val file = appConf.photosDir.resolve(path).toFile
    if (!file.isFile) NotFound(s"Not found: $path")
    else {
      contentType = Extensions.contentType(path)
      response.setContentLengthLong(file.length())
      file
    }
  }

  post("/*") {
    val path = multiParams("splat").head
    val content = fileParams("file").get()
    Files.write(appConf.photosDir.resolve(path), content)
  }

  put("/*") {
    val path = multiParams("splat").head
    val content = fileParams("file").get()
    Files.write(
      appConf.photosDir.resolve(path),
      content,
      StandardOpenOption.TRUNCATE_EXISTING,
      StandardOpenOption.WRITE
    )
  }

  error {
    case _: SizeConstraintExceededException => RequestEntityTooLarge("too much!")
  }
}
