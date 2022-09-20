package com.ponkotuy.app

import com.ponkotuy.db.{Image, ImageWithAll}
import com.ponkotuy.res.{Pagination, PagingResponse}
import org.scalatra.*
import scalikejdbc.*
import io.circe.*
import io.circe.generic.auto.*
import io.circe.parser.*
import io.circe.syntax.*

import java.time.LocalDate
import java.time.format.DateTimeFormatter

class PhotographicIndexer extends ScalatraServlet with CORSSetting with Pagination {
  before() {
    contentType = "application/json; charset=utf-8"
  }

  get("/images/:id"){
    val id = params("id").toLong
    ImageWithAll.find(id)(DB.readOnlySession()).asJson.noSpaces
  }

  get("/images/search") {
    implicit val session: DBSession = DB.readOnlySession()
    val q = params("q")
    paging { page => Image.searchAddress(q, page) } { Image.searchAddressCount(q) }
  }

  get("/images/date/:date") {
    val date = LocalDate.parse(params("date"), DateTimeFormatter.ISO_LOCAL_DATE)
    ImageWithAll.findFromDate(date)(DB.readOnlySession()).asJson.noSpaces
  }
}
