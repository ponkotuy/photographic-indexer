package com.ponkotuy.app

import com.ponkotuy.config.AppConfig
import com.ponkotuy.db.{ ImageFile, ImageWithAll }
import com.ponkotuy.util.{ Extensions, RawPreviewExtractor }
import org.scalatra._
import scalikejdbc.AutoSession

import java.nio.file.Path

class PublicStaticFile(appConf: AppConfig) extends ScalatraServlet with CORSSetting {
  get("/images/:id") {
    val id = params("id").toLong
    ImageWithAll.find(id, isPublic = true)(AutoSession).map { image =>
      val file = image.files.filterNot(_.isRetouch).minBy(_.filesize)
      val path = imagePath(file)
      if (Extensions.isRawFile(file.path)) {
        RawPreviewExtractor.largest(path).map { preview =>
          contentType = "image/jpeg"
          response.setContentLengthLong(preview.length)
          preview
        }.getOrElse(NotFound(s"Not found RAW preview image ${ id }"))
      } else {
        val f = path.toFile
        response.setContentLengthLong(f.length())
        f
      }
    }.getOrElse(NotFound(s"Not found image ${ id }"))
  }

  def imagePath(file: ImageFile): Path = appConf.photosDir.resolve(file.path.tail)
}
