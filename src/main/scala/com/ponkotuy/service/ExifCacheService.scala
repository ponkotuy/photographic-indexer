package com.ponkotuy.service

import com.ponkotuy.batch.{ Exif, ExifDetail, ExifParser }
import com.ponkotuy.db.{ ExifCache, Image }
import com.ponkotuy.util.Extensions
import org.gbif.common.parsers.geospatial.LatLng
import scalikejdbc.DBSession

import java.nio.file.Paths
import java.time.LocalDateTime

object ExifCacheService {
  def getOrElseUpdate(image: Image)(implicit session: DBSession): ExifCache = {
    image.exif.getOrElse {
      val result = for {
        file <- image.files.find(file => Extensions.isRawFile(file.path))
        path = Paths.get(file.path)
        base <- ExifParser.parse(path)
        detail = ExifParser.parseDetail(path)
      } yield buildCache(imageId = image.id, base = base, detail = detail)
      result.fold(throw new Exception("EXIF could not be retrieved.")) { x =>
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
