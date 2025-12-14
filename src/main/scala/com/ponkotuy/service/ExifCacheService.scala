package com.ponkotuy.service

import com.ponkotuy.batch.{ Exif, ExifDetail, ExifParser }
import com.ponkotuy.db.{ ExifCache, Image }
import com.ponkotuy.util.Extensions
import scalikejdbc.DBSession

import java.nio.file.Paths
import java.time.LocalDateTime

object ExifCacheService {
  def getOrElseUpdate(imageId: Long, file: String)(implicit session: DBSession): ExifCache = {
    ExifCache.find(imageId).getOrElse( create(imageId, file) )
  }
  def getOrElseUpdate(image: Image)(implicit session: DBSession): ExifCache = {
    image.exif.getOrElse { create(image) }
  }
  
  def create(imageId: Long, file: String)(implicit session: DBSession): ExifCache = {
    val path = Paths.get(file)
    val result = ExifParser.parse(path).map { base =>
      val detail = ExifParser.parseDetail(path)
      buildCache(imageId, base, detail)
    }
    result.fold(throw new Exception("EXIF could not be retrieved.")){ x =>
      ExifCache.create(
        x.imageId,
        x.serialNo,
        x.shotId,
        x.shootingAt,
        x.latitude,
        x.longitude,
        x.camera,
        x.lens,
        x.focalLength,
        x.aperture,
        x.exposure,
        x.iso,
        x.createdAt
      )
      x
    }
  }

  def create(image: Image)(implicit session: DBSession): ExifCache = {
    val result = for {
      file <- image.files.find(file => Extensions.isRawFile(file.path))
      path = Paths.get(file.path)
      base <- ExifParser.parse(path)
      detail = ExifParser.parseDetail(path)
    } yield buildCache(imageId = image.id, base = base, detail = detail)
    result.fold(throw new Exception("EXIF could not be retrieved.")){ x =>
      ExifCache.create(
        x.imageId,
        x.serialNo,
        x.shotId,
        x.shootingAt,
        x.latitude,
        x.longitude,
        x.camera,
        x.lens,
        x.focalLength,
        x.aperture,
        x.exposure,
        x.iso,
        x.createdAt
      )
      x
    }
  }

  private def buildCache(
      imageId: Long,
      base: Exif,
      detail: Option[ExifDetail],
      now: LocalDateTime = LocalDateTime.now()
  ) =
    ExifCache(
      imageId: Long,
      base.serialNo,
      base.shotId,
      base.shootingAt,
      base.latLon.map(_.getLat),
      base.latLon.map(_.getLng),
      base.camera,
      detail.flatMap(_.lens),
      detail.flatMap(_.focal),
      detail.flatMap(_.aperture),
      detail.map(_.exposureTime),
      detail.map(_.iso),
      now
    )
}
