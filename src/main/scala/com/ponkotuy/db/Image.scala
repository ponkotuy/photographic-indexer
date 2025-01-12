package com.ponkotuy.db

import com.ponkotuy.batch.ExifDetail
import scalikejdbc.*

import java.time.LocalDateTime
import scala.util.Try
import scala.util.control.NonFatal

case class Image(
    id: Long,
    cameraId: Int,
    shotId: Long,
    shootingAt: LocalDateTime,
    isPublic: Boolean = false,
    note: Option[String] = None,
    files: Seq[ImageFile] = Nil,
    tags: Seq[Tag] = Nil,
    geo: Option[Geom] = None,
    exif: Option[ExifDetail] = None,
    clipIndex: Option[ImageClipIndex] = None,
    flickr: Option[FlickrImage] = None
)

case class ImageRaw(
    id: Long,
    cameraId: Int,
    shotId: Long,
    shootingAt: LocalDateTime,
    geoId: Option[Long] = None,
    isPublic: Boolean = false,
    note: Option[String] = None
) {
  def toImage(geom: Option[Geom] = None, flickr: Option[FlickrImage] = None): Image =
    Image(id, cameraId, shotId, shootingAt, geo = geom, isPublic = isPublic, note = note, flickr = flickr)
}

object Image extends SQLSyntaxSupport[ImageRaw] {
  val i = Image.syntax("i")

  def apply(rn: ResultName[ImageRaw])(rs: WrappedResultSet): ImageRaw =
    autoConstruct(rs, rn)

  def applyWithGeom(im: ResultName[ImageRaw], g: ResultName[Geom])(rs: WrappedResultSet): Image = {
    val imResult = apply(im)(rs)
    val gResult = Try { Geom.apply(g)(rs) }.toOption
    imResult.toImage(geom = gResult)
  }

  def applyWithFlickr(im: ResultName[ImageRaw], f: ResultName[FlickrImage])(rs: WrappedResultSet): Image = {
    val imResult = apply(im)(rs)
    val fResult = Try { FlickrImage.apply(f)(rs) }.toOption
    imResult.toImage(flickr = fResult)
  }

  def find(cameraId: Int, shotId: Long)(implicit session: DBSession): Option[ImageRaw] = withSQL {
    select.from(Image as i).where.eq(i.cameraId, cameraId).and.eq(i.shotId, shotId)
  }.map(Image(i.resultName)).single.apply()

  def months()(implicit session: DBSession): Seq[String] = withSQL {
    select(sqls.distinct(sqls"date_format(${ i.shootingAt }, '%Y%m') as months")).from(Image as i)
      .orderBy(sqls"months".desc)
  }.map(rs => rs.string(1)).list.apply()

  def create(
      cameraId: Int,
      shotId: Long,
      shootingAt: LocalDateTime,
      geoId: Option[Long] = None,
      isPublic: Boolean = false,
      note: Option[String] = None
  )(implicit session: DBSession): Long = {
    withSQL {
      insert.into(Image).namedValues(
        column.cameraId -> cameraId,
        column.shotId -> shotId,
        column.shootingAt -> shootingAt,
        column.geoId -> geoId,
        column.isPublic -> isPublic,
        column.note -> note
      )
    }.updateAndReturnGeneratedKey.apply()
  }

  def save(id: Long, isPublic: Boolean, note: Option[String])(implicit session: DBSession): Int = withSQL {
    update(Image).set(column.isPublic -> isPublic, column.note -> note).where.eq(column.id, id)
  }.update.apply()

  def updatePublic(id: Long, isPublic: Boolean)(implicit session: DBSession): Int = withSQL {
    update(Image).set(column.isPublic -> isPublic).where.eq(column.id, id)
  }.update.apply()

  def updateNote(id: Long, note: Option[String])(implicit session: DBSession): Int = withSQL {
    update(Image).set(column.note -> note).where.eq(column.id, id)
  }.update.apply()

  def updateShootingAt(id: Long, shootingAt: LocalDateTime)(implicit session: DBSession): Int = withSQL {
    update(Image).set(column.shootingAt -> shootingAt).where.eq(column.id, id)
  }.update.apply()

  def remove(id: Long)(implicit session: DBSession): Int = applyUpdate {
    delete.from(Image).where.eq(column.id, id)
  }
}

case class CreateImage(
    cameraId: Int,
    shotId: Long,
    shootingAt: LocalDateTime,
    address: Option[String],
    lat: Option[Double],
    lon: Option[Double],
    isPublic: Boolean = false,
    note: Option[String] = None
) {
  def create(): Long = DB localTx { implicit session =>
    try {
      val geomId = for {
        address <- address
        lat <- lat
        lon <- lon
      } yield Geom.create(address, lat, lon)
      Image.create(cameraId, shotId, shootingAt, geomId, isPublic, note)
    } catch {
      case NonFatal(e) =>
        e.printStackTrace()
        throw e
    }
  }
}
