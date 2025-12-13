package com.ponkotuy.db

import org.apache.commons.math3.fraction.Fraction
import org.gbif.common.parsers.geospatial.LatLng
import scalikejdbc.*

import java.time.LocalDateTime

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

object ExifCache extends SQLSyntaxSupport[ExifCache] {
  override val tableName = "exif_cache"
  val ec = ExifCache.syntax("ec")

  def apply(rn: ResultName[ExifCache])(rs: WrappedResultSet): ExifCache = {
    val exposureNumerator = rs.intOpt(rn.field("exposureNumerator"))
    val exposureDenominator = rs.intOpt(rn.field("exposureDenominator"))
    val exposure = for {
      num <- exposureNumerator
      denom <- exposureDenominator
    } yield new Fraction(num, denom)

    ExifCache(
      imageId = rs.long(rn.imageId),
      serialNo = rs.intOpt(rn.serialNo),
      shotId = rs.longOpt(rn.shotId),
      shootingAt = rs.localDateTime(rn.shootingAt),
      latitude = rs.doubleOpt(rn.latitude),
      longitude = rs.doubleOpt(rn.longitude),
      camera = rs.string(rn.camera),
      lens = rs.stringOpt(rn.lens),
      focalLength = rs.intOpt(rn.focalLength),
      aperture = rs.bigDecimalOpt(rn.aperture).map(BigDecimal(_)),
      exposure = exposure,
      iso = rs.intOpt(rn.iso),
      createdAt = rs.localDateTime(rn.createdAt)
    )
  }

  def find(imageId: Long)(implicit session: DBSession): Option[ExifCache] = withSQL {
    select.from(ExifCache as ec).where.eq(ec.imageId, imageId)
  }.map(ExifCache(ec.resultName)).single.apply()

  def findAll(imageIds: Seq[Long])(implicit session: DBSession): Seq[ExifCache] = withSQL {
    select.from(ExifCache as ec).where.in(ec.imageId, imageIds)
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
