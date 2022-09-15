package com.ponkotuy.db

import com.ponkotuy.res.Paging

import java.time.LocalDateTime
import scalikejdbc.*
import scalikejdbc.sqls.{count, distinct}

import scala.util.Try
import scala.util.control.NonFatal

case class Image(id: Long, cameraId: Int, shotId: Int, shootingAt: LocalDateTime, geo: Option[Geom] = None)
case class ImageRaw(id: Long, cameraId: Int, shotId: Int, shootingAt: LocalDateTime, geoId: Option[Long] = None) {
  def toImage(geom: Option[Geom]): Image = Image(id, cameraId, shotId, shootingAt, geom)
}

object Image extends SQLSyntaxSupport[ImageRaw] {
  import Geom.g
  val i = Image.syntax("i")

  def apply(rn: ResultName[ImageRaw])(rs: WrappedResultSet): ImageRaw =
    autoConstruct(rs, rn)

  def applyWithGeom(im: ResultName[ImageRaw], g: ResultName[Geom])(rs: WrappedResultSet): Image = {
    val imResult = autoConstruct(rs, im)
    val gResult = Try { Geom.apply(g)(rs) }.toOption
    imResult.toImage(gResult)
  }

  def find(cameraId: Int, shotId: Int)(implicit session: DBSession): Option[ImageRaw] = withSQL {
    select.from(Image as i).where.eq(i.cameraId, cameraId).and.eq(i.shotId, shotId)
  }.map(Image(i.resultName)).single.apply()

  def searchAddress(text: String, paging: Paging = Paging.NoLimit)(implicit session: DBSession): List[Image] = withSQL {
    select(i.resultAll +: Geom.select:_*).from(Image as i).innerJoin(Geom as g).on(i.geoId, g.id)
        .where(sqls"match (${g.address}) against (${text} in natural language mode)")
        .limit(paging.limit).offset(paging.offset)
  }.map(Image.applyWithGeom(i.resultName, g.resultName)).list.apply()

  def searchAddressCount(text: String)(implicit session: DBSession): Long = withSQL {
    select(count(distinct(i.id))).from(Image as i).innerJoin(Geom as g).on(i.geoId, g.id)
        .where(sqls"match (${g.address}) against (${text} in natural language mode)")
  }.map(_.int(1)).single.apply().get

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
