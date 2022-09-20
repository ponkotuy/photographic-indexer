package com.ponkotuy.db

import scalikejdbc.*

import java.time.{LocalDate, LocalDateTime}
import scala.util.Try

case class ImageWithAll(
    id: Long,
    cameraId: Int,
    shotId: Int,
    shootingAt: LocalDateTime,
    file: ImageFile,
    geo: Option[Geom]
) {
  def toImage: Image = Image(id, cameraId, shotId, shootingAt, file :: Nil, geo)
}

object ImageWithAll {
  import Image.i
  import Geom.g
  import ImageFile.imf

  private[this] val selectAll: Seq[SQLSyntax] = i.resultAll +: imf.resultAll +: Geom.select
  private[this] val selectWithJoin = select(selectAll: _*).from(Image as i)
      .leftJoin(Geom as g).on(i.geoId, g.id)
      .innerJoin(ImageFile as imf).on(i.id, imf.imageId)

  def apply(rs: WrappedResultSet): ImageWithAll = {
    val imResult = autoConstruct(rs, i.resultName)
    val gResult = Try { Geom.apply(g.resultName)(rs) }.toOption
    val imfResult = ImageFile.apply(imf.resultName)(rs)
    imResult.toWithAll(gResult, imfResult)
  }

  def find(id: Long)(implicit session: DBSession): Option[Image] = {
    val result: List[ImageWithAll] = withSQL {
      selectWithJoin.where.eq(i.id, id)
    }.map(apply).list.apply()
    result.headOption.map(_.toImage.copy(files = result.map(_.file)))
  }

  def findFromDate(date: LocalDate)(implicit session: DBSession): Seq[Image] = {
    val result: List[ImageWithAll] = withSQL {
      selectWithJoin
          .where.between(i.shootingAt, date.atStartOfDay(), date.plusDays(1).atStartOfDay())
          .orderBy(i.shootingAt)
    }.map(apply).list.apply()
    result.view.groupBy(_.id).map { (_, xs) =>
      xs.head.toImage.copy(files = xs.map(_.file).toSeq)
    }.toSeq
  }
}
