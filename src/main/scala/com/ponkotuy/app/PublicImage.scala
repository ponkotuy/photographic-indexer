package com.ponkotuy.app

import com.ponkotuy.config.AppConfig
import com.ponkotuy.db.ImageWithAll
import com.ponkotuy.res.Pagination
import com.ponkotuy.service.ImageService
import io.circe.*
import io.circe.generic.auto.*
import io.circe.syntax.*
import org.scalatra.ScalatraServlet
import scalikejdbc.DB

class PublicImage(app: AppConfig)
    extends ScalatraServlet
    with Pagination
    with CORSSetting {
  import com.ponkotuy.util.CustomEncoder.fraction

  val imageService = new ImageService(app.photosDir)

  before() {
    contentType = "application/json; charset=utf-8"
  }

  get("/") {
    DB.readOnly { implicit session =>
      paging { page =>
        ImageWithAll.findAll(ImageWithAll.isPublicSQL, limit = page.limit, offset = page.offset)
      } {
        ImageWithAll.findAllCount(ImageWithAll.isPublicSQL)
      }
    }
  }

  get("/:id") {
    val id = params("id").toLong
    val withExif = params.get("exif").exists(_.toBoolean)
    val result = imageService.findImage(id, isPublic = true, withExif).asJson.noSpaces
    result
  }

  get("/random") {
    DB.localTx { implicit session =>
      val withExif = params.get("exif").exists(_.toBoolean)
      val image = ImageWithAll.findRandom(ImageWithAll.isPublicSQL)
      println(image)
      image.map(imageService.setExif).asJson.noSpaces
    }
  }
}
