package com.ponkotuy.app

import com.ponkotuy.config.{AppConfig, MyConfig}
import com.ponkotuy.db.{ImageFile, ImageWithAll}
import org.scalatra._
import scalikejdbc.AutoSession

import java.nio.file.Path

class PublicStaticFile(appConf: AppConfig) extends ScalatraServlet with CORSSetting {
  get("/images/:id") {
    val id = params("id").toLong
    ImageWithAll.find(id)(AutoSession).map { image =>
      val file = image.files.filterNot(_.isRetouch).minBy(_.filesize)
      imagePath(file).toFile
    }.getOrElse(NotFound(s"Not found image ${id}"))
  }

  def imagePath(file: ImageFile): Path = appConf.photosDir.resolve(file.path.tail)
}
