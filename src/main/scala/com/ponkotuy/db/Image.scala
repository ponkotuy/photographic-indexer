package com.ponkotuy.db

import java.time.LocalDateTime
import scalikejdbc._

import scala.util.control.NonFatal

case class Image(id: Long, cameraId: Int, shotId: Int, shootingAt: LocalDateTime, geo: Option[Geom] = None)
case class ImageRaw(id: Long, cameraId: Int, shotId: Int, shootingAt: LocalDateTime, geoId: Option[Long] = None)

object Image extends SQLSyntaxSupport[ImageRaw] {
  val i = Image.syntax("i")

  def apply(rn: ResultName[ImageRaw])(rs: WrappedResultSet): ImageRaw =
    autoConstruct(rs, rn)

  def find(cameraId: Int, shotId: Int)(implicit session: DBSession): Option[ImageRaw] = withSQL {
    select.from(Image as i).where.eq(i.cameraId, cameraId).and.eq(i.shotId, shotId)
  }.map(Image(i.resultName)).single.apply()

  def create(cameraId: Int, shotId: Int, shootingAt: LocalDateTime, geoId: Option[Long] = None)(implicit session: DBSession): Long = {
    withSQL {
      insert.into(Image).namedValues(
        column.cameraId -> cameraId,
        column.shotId -> shotId,
        column.shootingAt -> shootingAt,
        column.geoId -> geoId
      )
    }.updateAndReturnGeneratedKey.apply()
  }
}

case class CreateImage(
    cameraId: Int,
    shotId: Int,
    shootingAt: LocalDateTime,
    address: Option[String],
    lat: Option[Double],
    lon: Option[Double]
) {
  def create(): Long = DB localTx { implicit session =>
    try {
      val geomId = for {
        address <- address
        lat <- lat
        lon <- lon
      } yield Geom.create(address, lat, lon)
      Image.create(cameraId, shotId, shootingAt, geomId)
    } catch {
      case NonFatal(e) =>
        e.printStackTrace()
        throw e
    }
  }
}
