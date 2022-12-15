package com.ponkotuy.app

import com.ponkotuy.config.AppConfig
import com.ponkotuy.db.ImageWithAll
import com.ponkotuy.res.Pagination
import org.scalatra.ScalatraServlet
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
        ImageWithAll.findAllPublic(page)
      } {
        ImageWithAll.findAllPublicCount()
      }
    }
  }
}
