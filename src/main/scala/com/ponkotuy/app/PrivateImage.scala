package com.ponkotuy.app

import com.ponkotuy.batch.{ExifParser, ThumbnailGenerator}
import com.ponkotuy.clip.ClipCache
import com.ponkotuy.config.{AppConfig, MyConfig}
import com.ponkotuy.db.{Image, ImageFile, ImageTag, ImageWithAll, Tag, Thumbnail}
import com.ponkotuy.req.{PutImageTag, PutNote, PutTag, SearchParams, SearchParamsGenerator}
import com.ponkotuy.res.{AggregateDate, DateCount, Pagination}
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

class PrivateImage(config: MyConfig)
    extends ScalatraServlet
        with CORSSetting
        with Pagination
        with SearchParamsGenerator
        with ParseJSON {
  import com.ponkotuy.util.CustomEncoder.fraction

  before() {
    contentType = "application/json; charset=utf-8"
  }

  get("/:id") {
    val id = params("id").toLong
    val withExif = params.get("exif").exists(_.toBoolean)
    DB.readOnly { implicit session =>
      ImageWithAll.find(id, isPublic = false).map { image =>
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
  delete("/:id") {
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

  put("/:id/note") {
    val id = params("id").toLong
    parseJson[PutNote]().map{ obj =>
      DB.localTx { implicit session =>
        Image.updateNote(id, obj.note.filterNot(_ == ""))
        Ok("Success")
      }
    }.merge
  }

  put("/:imageId/tag/:tagId") {
    val imageId = params("imageId").toLong
    val tagId = params("tagId").toLong
    DB.localTx { implicit session =>
      ImageTag.create(imageId, tagId)
    }
    Ok("Success")
  }

  delete("/:imageId/tag/:tagId") {
    val imageId = params("imageId").toLong
    val tagId = params("tagId").toLong
    DB.localTx { implicit session =>
      ImageTag.remove(imageId, tagId)
      Ok("Success")
    }
  }

  private[this] val generator = ThumbnailGenerator(960, 640)

  get("/:id/thumbnail") {
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

  put("/:id/public") {
    val id = params("id").toLong
    Image.updatePublic(id, true)(AutoSession)
    Ok("Success")
  }

  delete("/:id/public") {
    val id = params("id").toLong
    Image.updatePublic(id, false)(AutoSession)
    Ok("Success")
  }

  get("/search") {
    DB.readOnly { implicit session =>
      val params = getSearchParams()
      val result = paging { page =>
        ImageWithAll.searchFulltext(params, page)
      }{
        ImageWithAll.searchFulltextCount(params)
      }
      result.asJson.noSpaces
    }
  }

  get("/search_date_count") {
    DB.readOnly { implicit session =>
      val params = getSearchParams()
      val result = ImageWithAll.searchFulltextDateCount(params)
      result.toVector.map((date, count) => DateCount(date, count)).sortBy(- _.count).take(5).asJson.noSpaces
    }
  }

  private[this] val clipOpt = config.clip.map(new ClipCache(_))

  get("/search_clip") {
    val result = for {
      clip <- clipOpt.toRight(InternalServerError("Not found clip settings"))
      text <- params.get("keyword").toRight(BadRequest("Required query parameter 'q'"))
      clipResult <- clip.search(text).map(_.filter(0 < _.score)).toRight(InternalServerError("Unknown Error(ClipCache#search return None)"))
    } yield {
      val pagingResult = paging { page =>
        val ids = clipResult.sortBy(-_.score).slice(page.perPage, page.perPage + page.limit).map(_.imageId)
        ImageWithAll.findAllInIds(ids)(AutoSession)
      } {
        clipResult.length.toLong
      }
      val dateCounts = pagingResult.data.groupBy(_.shootingAt.toLocalDate)
          .toVector
          .map((date, images) => DateCount(date, images.length)).sortBy(-_.count).take(5)
      pagingResult.withDateCounts(dateCounts).asJson.noSpaces
    }
    result.merge
  }

  get("/date/:date") {
    import com.ponkotuy.db.Image.i
    val isPublic = params.get("isPublic").flatMap(_.toBooleanOption).getOrElse(false)
    val date = LocalDate.parse(params("date"), DateTimeFormatter.ISO_LOCAL_DATE)
    val cond = ImageWithAll.aDay(date)
        .and(if(isPublic) Some(sqls.eq(i.isPublic, true)) else None)
    DB.readOnly { implicit session =>
      ImageWithAll.findAll(cond).asJson.noSpaces
    }
  }

  get("/tags") {
    DB.readOnly { implicit session =>
      Tag.findAll().asJson.noSpaces
    }
  }

  put("/tags") {
    parseJson[PutTag]().map { tag =>
      DB.localTx { implicit session =>
        Tag.create(tag.name)
        Ok("Success")
      }
    }.merge
  }

  get("/calendar/:month") {
    val month = YearMonth.parse(params("month"), monthFormatter)
    DB.readOnly { implicit session =>
      ImageWithAll.aggregateMonthlyByDate(month)
        .map { (date, images) => AggregateDate.fromImages(date, images) }
        .toVector
        .sortBy(_.date)
        .asJson.noSpaces
    }
  }

  get("/calendar/months") {
    DB.readOnly { implicit session =>
      Image.months().asJson.noSpaces
    }
  }

  def imagePath(file: ImageFile): Path = config.app.photosDir.resolve(file.path.tail)
}
