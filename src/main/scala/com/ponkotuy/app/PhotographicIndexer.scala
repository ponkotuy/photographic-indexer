package com.ponkotuy.app

import com.ponkotuy.batch.ThumbnailGenerator
import com.ponkotuy.config.AppConfig
import com.ponkotuy.db.{Image, ImageFile, ImageWithAll, Thumbnail}
import com.ponkotuy.req.{SearchParams, SearchParamsGenerator}
import com.ponkotuy.res.{DateCount, Pagination, PagingResponse}
import org.scalatra.*
import scalikejdbc.*
import io.circe.*
import io.circe.generic.auto.*
import io.circe.parser.*
import io.circe.syntax.*

import java.time.LocalDate
import java.time.format.DateTimeFormatter

class PhotographicIndexer(appConfig: AppConfig)
    extends ScalatraServlet
        with CORSSetting
        with Pagination
        with SearchParamsGenerator {
  before() {
    contentType = "application/json; charset=utf-8"
  }

  get("/images/:id") {
    val id = params("id").toLong
    DB.readOnly { implicit session =>
      ImageWithAll.find(id).asJson.noSpaces
    }
  }

  private[this] val generator = ThumbnailGenerator(960, 640)

  get("/images/:id/thumbnail") {
    contentType = "image/jpeg"
    val id = params("id").toLong
    implicit val session: DBSession = AutoSession
    Thumbnail.find(id).map(_.file).getOrElse {
      val file = ImageFile.findAllInImageIds(id :: Nil).filterNot(_.isRetouch).minBy(_.filesize)
      val binary = generator.gen(appConfig.photosDir.resolve(file.path.tail))
      Thumbnail.create(id, binary)
      binary
    }
  }

  get("/images/search") {
    val params = getSearchParams()
    DB.readOnly { implicit session =>
      paging { page =>
        ImageWithAll.searchFulltext(params, page)
      }{
        ImageWithAll.searchFulltextCount(params)
      }
    }
  }

  get("/images/search_date_count") {
    val params = getSearchParams()
    DB.readOnly { implicit session =>
      val result = ImageWithAll.searchFulltextDateCount(params)
      result.toVector.map((date, count) => DateCount(date, count)).sortBy(- _.count).take(5).asJson.noSpaces
    }
  }

  get("/images/date/:date") {
    val date = LocalDate.parse(params("date"), DateTimeFormatter.ISO_LOCAL_DATE)
    DB.readOnly { implicit session =>
      ImageWithAll.findFromDate(date).asJson.noSpaces
    }
  }
}
