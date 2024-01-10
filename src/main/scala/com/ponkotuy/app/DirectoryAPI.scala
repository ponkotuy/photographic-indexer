package com.ponkotuy.app

import com.ponkotuy.config.AppConfig
import com.ponkotuy.db.ImageFile
import com.ponkotuy.res.{ FileElement, FileType }
import org.scalatra.ScalatraServlet
import scalikejdbc.AutoSession
import io.circe.*
import io.circe.generic.auto.*
import io.circe.parser.*
import io.circe.syntax.*

import java.nio.file.Files
import scala.jdk.StreamConverters.*

class DirectoryAPI(appConf: AppConfig) extends ScalatraServlet with CORSSetting {
  before() {
    contentType = "application/json; charset=utf-8"
  }

  // return file list 'ordered'
  get("/*") {
    val path = multiParams("splat").head
    val abs = appConf.photosDir.resolve(path)
    Files.list(abs).toScala(Seq).flatMap { elem =>
      if(Files.isDirectory(elem)) {
        Some(FileElement(FileType.Directory, elem.getFileName.toString))
      } else {
        val rel = "/" + appConf.photosDir.relativize(elem)
        val imgFile = ImageFile.findFromPath(rel)(AutoSession)
        imgFile.map(img => FileElement(FileType.File, elem.getFileName.toString, Some(img.imageId)))
      }
    }.sortBy(f => (f.fileType, f.name)).asJson
  }
}


