package com.ponkotuy.app

import com.ponkotuy.db.Image
import com.ponkotuy.res.{Pagination, PagingResponse}
import org.scalatra.*
import scalikejdbc.*
import io.circe.*
import io.circe.generic.auto.*
import io.circe.parser.*
import io.circe.syntax.*

class PhotographicIndexer extends ScalatraServlet with CORSSetting with Pagination {
  before() {
    contentType = "application/json; charset=utf-8"
  }

  get("/images/search") {
    implicit val session: DBSession = DB.readOnlySession()
    val q = params("q")
    paging { page => Image.searchAddress(q, page) } { Image.searchAddressCount(q) }
  }
}
