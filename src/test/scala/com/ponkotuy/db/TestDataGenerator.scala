package com.ponkotuy.db

import org.apache.commons.math3.fraction.Fraction
import scalikejdbc.*

import java.time.LocalDateTime
import scala.util.Random

object TestDataGenerator {
  def image(
      id: Long = 0L,
      fileId: Long = 0L,
      cameraId: Int = 2,
      shotId: Long = Random.nextLong(),
      shootingAt: LocalDateTime = LocalDateTime.now(),
      geoId: Option[Long] = None,
      isPublic: Boolean = false,
      note: Option[String] = None
  )(implicit session: DBSession): Long = {
    import Image.column
    val imageId = withSQL {
      insert.into(Image).namedValues(
        column.id -> id,
        column.cameraId -> cameraId,
        column.shotId -> shotId,
        column.shootingAt -> shootingAt,
        column.geoId -> geoId,
        column.isPublic -> isPublic,
        column.note -> note
      )
    }.updateAndReturnGeneratedKey.apply()
    imageFile(imageId = imageId, id = fileId)
    imageCache(imageId = imageId)
    imageId
  }

  def imageFile(
      imageId: Long,
      id: Long = 0L,
      path: String = s"/${ Random.alphanumeric.take(10).mkString }/${ Random.alphanumeric.take(6).mkString }.jpg",
      filesize: Long = 102400L
  )(implicit session: DBSession): Long = {
    import ImageFile.column
    withSQL {
      insert.into(ImageFile).namedValues(
        column.id -> id,
        column.imageId -> imageId,
        column.path -> path,
        column.filesize -> filesize
      )
    }.updateAndReturnGeneratedKey.apply()
  }

  def imageCache(
      imageId: Long,
      serialNo: Option[Int] = None,
      shotId: Option[Long] = None,
      shootingAt: LocalDateTime = LocalDateTime.now(),
      latitude: Option[Double] = None,
      longitude: Option[Double] = None,
      camera: String = "Nikon Z7",
      lens: Option[String] = None,
      focalLength: Option[Int] = None,
      aperture: Option[BigDecimal] = None,
      exposure: Option[Fraction] = None,
      iso: Option[Int] = None,
      createdAt: LocalDateTime = LocalDateTime.now()
  )(implicit session: DBSession): Unit = applyUpdate {
    import ExifCache.column
    insert.into(ExifCache).namedValues(
      column.imageId -> imageId,
      column.serialNo -> serialNo,
      column.shotId -> shotId,
      column.shootingAt -> shootingAt,
      column.latitude -> latitude,
      column.longitude -> longitude,
      column.camera -> camera,
      column.lens -> lens,
      column.focalLength -> focalLength,
      column.aperture -> aperture,
      column.field("exposureNumerator") -> exposure.map(_.getNumerator),
      column.field("exposureDenominator") -> exposure.map(_.getDenominator),
      column.iso -> iso,
      column.createdAt -> createdAt
    )
  }
}
