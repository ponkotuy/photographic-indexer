package com.ponkotuy.res

import io.circe.*
import io.circe.generic.auto.*
import io.circe.parser.*
import io.circe.syntax.*
import io.circe.Encoder.AsArray.importedAsArrayEncoder
import org.scalatra.ScalatraServlet

case class PagingResponse[T](data: Seq[T], page: Int, perPage: Int, allCount: Long)

case class Paging(page: Int, perPage: Int) {
  def limit: Int = perPage
  def offset: Int = page * perPage
}

object Paging {
  val NoLimit: Paging = Paging(0, Int.MaxValue)
}

trait Pagination { self: ScalatraServlet =>
  def paging[T](dataQuery: Paging => Seq[T])(countQuery: => Long)(implicit encoder: Encoder[PagingResponse[T]]): String = {
    val page = params.get("page").flatMap(_.toIntOption).getOrElse(0)
    val perPage = params.get("perPage").flatMap(_.toIntOption).getOrElse(20)
    assert(perPage <= 100 && (page + 1) * perPage <= 3000, "Pagination Limit Over")
    val allCount = countQuery
    val data = dataQuery(Paging(page, perPage))
    PagingResponse(data, page, perPage, allCount).asJson.noSpaces
  }
}
