package com.ponkotuy.db

import org.apache.commons.math3.fraction.Fraction
import scalikejdbc.*

import java.time.LocalDateTime

private case class ExifCacheDB(
    imageId: Long,
    serialNo: Option[Int],
    shotId: Option[Long],
    shootingAt: LocalDateTime,
    latitude: Option[Double],
    longitude: Option[Double],
    camera: String,
    lens: Option[String],
    focalLength: Option[Int],
    aperture: Option[BigDecimal],
    exposureNumerator: Option[Int],
    exposureDenominator: Option[Int],
    iso: Option[Int],
    createdAt: LocalDateTime
)

case class ExifCache(
    imageId: Long,
    serialNo: Option[Int],
    shotId: Option[Long],
    shootingAt: LocalDateTime,
    latitude: Option[Double],
    longitude: Option[Double],
    camera: String,
    lens: Option[String],
    focalLength: Option[Int],
    aperture: Option[BigDecimal],
    exposure: Option[Fraction],
    iso: Option[Int],
    createdAt: LocalDateTime
)

object ExifCache extends SQLSyntaxSupport[ExifCacheDB] {
  override val tableName = "exif_cache"
  val ec = ExifCache.syntax("ec")

  def apply(rn: ResultName[ExifCacheDB])(rs: WrappedResultSet): ExifCache = {
    val x = autoConstruct(rs, rn)
    fromDB(x)
  }

  def fromDB(x: ExifCacheDB): ExifCache = {
    val exposure = for {
      num <- x.exposureNumerator
      denom <- x.exposureDenominator
    } yield new Fraction(num, denom)
    ExifCache(
      imageId = x.imageId,
      serialNo = x.serialNo,
      shotId = x.shotId,
      shootingAt = x.shootingAt,
      latitude = x.latitude,
      longitude = x.longitude,
      camera = x.camera,
      lens = x.lens,
      focalLength = x.focalLength,
      aperture = x.aperture,
      exposure = exposure,
      iso = x.iso,
      createdAt = x.createdAt
    )
  }

  def find(imageId: Long)(implicit session: DBSession): Option[ExifCache] = withSQL {
    select.from(ExifCache as ec).where.eq(ec.imageId, imageId)
  }.map(ExifCache(ec.resultName)).single.apply()

  def findAll(where: Option[SQLSyntax] = None)(implicit session: DBSession): Seq[ExifCache] = withSQL {
    select.from(ExifCache as ec).where(where)
  }.map(ExifCache(ec.resultName)).list.apply()

  def create(
      imageId: Long,
      serialNo: Option[Int],
      shotId: Option[Long],
      shootingAt: LocalDateTime,
      latitude: Option[Double],
      longitude: Option[Double],
      camera: String,
      lens: Option[String],
      focalLength: Option[Int],
      aperture: Option[BigDecimal],
      exposure: Option[Fraction],
      iso: Option[Int],
      createdAt: LocalDateTime = LocalDateTime.now()
  )(implicit session: DBSession): Unit = applyUpdate {
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

  def exists(imageId: Long)(implicit session: DBSession): Boolean =
    find(imageId).isDefined

  def remove(imageId: Long)(implicit session: DBSession): Int = applyUpdate {
    delete.from(ExifCache).where.eq(column.imageId, imageId)
  }
}
