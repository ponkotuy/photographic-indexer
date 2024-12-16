package com.ponkotuy.app

import com.ponkotuy.config.AppConfig
import com.ponkotuy.db.ImageWithAll
import com.ponkotuy.res.Pagination
import org.scalatra.{ NotFound, Ok, ScalatraServlet }
import scalikejdbc.DB
import io.circe.*
import io.circe.generic.auto.*
import io.circe.parser.*
import io.circe.syntax.*

class PublicImage()
    extends ScalatraServlet
        with Pagination
        with CORSSetting {
  import com.ponkotuy.util.CustomEncoder.fraction

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
    DB.readOnly { implicit session =>
      ImageWithAll.find(id, isPublic = true).asJson.noSpaces
    }
  }

  get("/random") {
    DB.readOnly { implicit session =>
      ImageWithAll.findRandom(ImageWithAll.isPublicSQL).asJson.noSpaces
    }
  }
}
