package com.ponkotuy.app

import com.ponkotuy.batch.{ExifParser, ThumbnailGenerator}
import com.ponkotuy.config.AppConfig
import com.ponkotuy.db.{Image, ImageFile, ImageTag, ImageWithAll, Tag, Thumbnail}
import com.ponkotuy.req.{PutImageTag, PutTag, SearchParams, SearchParamsGenerator}
import com.ponkotuy.res.{AggregateDate, DateCount, Pagination, PagingResponse}
import com.ponkotuy.util.Extensions.{isImageFile, isRawFile}
import com.ponkotuy.util.CustomFormatter.monthFormatter
import org.scalatra.*
import scalikejdbc.*
import io.circe.*
import io.circe.generic.auto.*
import io.circe.parser.*
import io.circe.syntax.*

import java.nio.file.{Files, Path}
import java.time.{LocalDate, YearMonth}
import java.time.format.DateTimeFormatter

class PhotographicIndexer(appConfig: AppConfig)
    extends ScalatraServlet
        with CORSSetting
        with Pagination
        with SearchParamsGenerator
        with ParseJSON {

  import com.ponkotuy.util.CustomEncoder.fraction

  before() {
    contentType = "application/json; charset=utf-8"
  }

  get("/images/:id") {
    val id = params("id").toLong
    val withExif = params.get("exif").exists(_.toBoolean)
    DB.readOnly { implicit session =>
      ImageWithAll.find(id).map { image =>
        if(withExif) {
          val result = for {
            file <- image.files.find(f => isRawFile(f.path))
                .orElse(image.files.find(f => isImageFile(f.path)))
            detail <- ExifParser.parseDetail(imagePath(file))
          } yield image.copy(exif = Some(detail))
          result.getOrElse(image)
        } else image
      }.asJson.noSpaces
    }
  }

  // Delete all files, db records
  delete("/images/:id") {
    val id = params("id").toLong
    DB.localTx { implicit session =>
      val files = ImageFile.findAllInImageIds(id :: Nil)
      files.foreach(f => ImageFile.remove(f.id))
      Image.remove(id)
      files.foreach { file =>
        Files.delete(imagePath(file))
      }
    }
  }

  put("/images/:imageId/tag/:tagId") {
    val imageId = params("imageId").toLong
    val tagId = params("tagId").toLong
    DB.localTx { implicit session =>
      ImageTag.create(imageId, tagId)
    }
    Ok("Success")
  }

  delete("/images/:imageId/tag/:tagId") {
    val imageId = params("imageId").toLong
    val tagId = params("tagId").toLong
    DB.localTx { implicit session =>
      ImageTag.remove(imageId, tagId)
      Ok("Success")
    }
  }

  private[this] val generator = ThumbnailGenerator(960, 640)

  get("/images/:id/thumbnail") {
    contentType = "image/jpeg"
    val id = params("id").toLong
    implicit val session: DBSession = AutoSession
    Thumbnail.find(id).map(_.file).getOrElse {
      val file = ImageFile.findAllInImageIds(id :: Nil).filterNot(_.isRetouch).minBy(_.filesize)
      val binary = generator.gen(imagePath(file))
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

  get("/images/tags") {
    DB.readOnly { implicit session =>
      Tag.findAll().asJson.noSpaces
    }
  }

  put("/images/tags") {
    parseJson[PutTag]().map { tag =>
      DB.localTx { implicit session =>
        Tag.create(tag.name)
        Ok("Success")
      }
    }.merge
  }

  get("/images/calendar/:month") {
    val month = YearMonth.parse(params("month"), monthFormatter)
    DB.readOnly { implicit session =>
      ImageWithAll.aggregateMonthlyByDate(month)
        .map { (date, images) => AggregateDate.fromImages(date, images) }
        .toVector
        .sortBy(_.date)
        .asJson.noSpaces
    }
  }

  def imagePath(file: ImageFile): Path = appConfig.photosDir.resolve(file.path.tail)
}
