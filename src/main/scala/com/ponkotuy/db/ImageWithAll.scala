package com.ponkotuy.db

import com.ponkotuy.res.Paging
import scalikejdbc.*
import scalikejdbc.sqls.{count, distinct}

import java.time.{LocalDate, LocalDateTime}
import scala.annotation.nowarn
import scala.util.Try

object ImageWithAll {
  import Image.i
  import Geom.g
  import ImageFile.imf

  private def groupConcat(sql: SQLSyntax, name: SQLSyntax) = sqls"group_concat(${sql}) as ${name}"
  private[this] val imfSelect = groupConcat(imf.id, sqls"imf_ids") ::
      groupConcat(imf.path, sqls"imf_paths") ::
      groupConcat(imf.filesize,sqls"imf_filesizes") :: Nil
  private[this] val selectAll = i.resultAll +: (imfSelect ++ Geom.select)
  private[this] val selectWithJoin = select(selectAll: _*).from(Image as i)
      .leftJoin(Geom as g).on(i.geoId, g.id)
      .innerJoin(ImageFile as imf).on(i.id, imf.imageId)

  def apply(rs: WrappedResultSet): Image = {
    val imResult = autoConstruct(rs, i.resultName)
    val gResult = Try{
      Geom.apply(g.resultName)(rs)
    }.toOption
    val ids = rs.string("imf_ids").split(',')
    val paths = rs.string("imf_paths").split(',')
    val filesizes = rs.string("imf_filesizes").split(',')
    @nowarn()
    val files = (ids :: paths :: filesizes :: Nil).transpose.map { case List(id, path, filesize) =>
      ImageFile(id.toLong, imResult.id, path, filesize.toLong)
    }
    imResult.toImage(gResult).copy(files = files)
  }

  def find(id: Long)(implicit session: DBSession): Option[Image] = withSQL {
      selectWithJoin.where.eq(i.id, id)
    }.map(apply).single.apply()

  def findFromDate(date: LocalDate)(implicit session: DBSession): Seq[Image] = withSQL {
      selectWithJoin
          .where.between(i.shootingAt, date.atStartOfDay(), date.plusDays(1).atStartOfDay())
          .orderBy(i.shootingAt)
    }.map(apply).list.apply()

  def searchAddress(text: String, paging: Paging = Paging.NoLimit)(implicit session: DBSession): Seq[Image] = {
    val fulltext = sqls"match (${g.address}) against (${text} in natural language mode)"
    withSQL {
      selectWithJoin
          .where(sqls.gt(fulltext, 0))
          .groupBy(i.id)
          .orderBy(fulltext.desc)
          .limit(paging.limit).offset(paging.offset)
    }.map(apply).list.apply()
  }

  def searchAddressCount(text: String)(implicit session: DBSession): Long = withSQL{
    select(count(distinct(i.id))).from(Image as i).innerJoin(Geom as g).on(i.geoId, g.id)
        .where(sqls"match (${g.address}) against (${text} in natural language mode)")
  }.map(_.int(1)).single.apply().get
}
