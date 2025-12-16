package com.ponkotuy.service

import com.ponkotuy.batch.{ Exif, ExifDetail, ExifParser }
import com.ponkotuy.db.{ ExifCache, Image }
import com.ponkotuy.util.Extensions
import scalikejdbc.DBSession

import java.nio.file.{ Path, Paths }
import java.time.LocalDateTime

object ExifCacheService {

  def getOrElseUpdate(imageId: Long, path: Path)(implicit session: DBSession): Option[ExifCache] = {
    ExifCache.find(imageId).orElse(createFromPath(imageId, path))
  }

  def getOrElseUpdate(image: Image, photosDir: Path)(implicit session: DBSession): Option[ExifCache] = {
    image.exif.orElse {
      for {
        file <- image.files.find(f => Extensions.isRawFile(f.path))
          .orElse(image.files.find(f => Extensions.isImageFile(f.path)))
        cache <- createFromPath(image.id, file.absolutePath(photosDir))
      } yield cache
    }
  }

  def createFromPath(imageId: Long, path: Path)(implicit session: DBSession): Option[ExifCache] = {
    for {
      base <- ExifParser.parse(path)
      detail = ExifParser.parseDetail(path)
      cache = buildCache(imageId, base, detail)
      _ = saveCache(cache)
    } yield cache
  }

  private def buildCache(
      imageId: Long,
      base: Exif,
      detail: Option[ExifDetail],
      now: LocalDateTime = LocalDateTime.now()
  ): ExifCache =
    ExifCache(
      imageId,
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

  private def saveCache(cache: ExifCache)(implicit session: DBSession): Unit = {
    ExifCache.create(
      cache.imageId,
      cache.serialNo,
      cache.shotId,
      cache.shootingAt,
      cache.latitude,
      cache.longitude,
      cache.camera,
      cache.lens,
      cache.focalLength,
      cache.aperture,
      cache.exposure,
      cache.iso,
      cache.createdAt
    )
  }
}
