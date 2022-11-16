package com.ponkotuy.db

import com.ponkotuy.req.SearchParams
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
  import ImageTag.it
  import Tag.t

  private def groupConcat(sql: SQLSyntax, name: SQLSyntax) = sqls"group_concat(${sql}) as ${name}"
  private[this] val imfSelect = groupConcat(imf.id, sqls"imf_ids") ::
      groupConcat(imf.path, sqls"imf_paths") ::
      groupConcat(imf.filesize,sqls"imf_filesizes") :: Nil
  private[this] val tSelect = groupConcat(t.id, sqls"t_ids") ::
      groupConcat(t.name, sqls"t_names") :: Nil
  private[this] val selectAll = i.resultAll +: (imfSelect ++ tSelect ++ Geom.select)
  private def selectWithJoin(where: SQLSyntax) = select(selectAll: _*).from(Image as i)
      .leftJoin(Geom as g).on(i.geoId, g.id)
      .innerJoin(ImageFile as imf).on(i.id, imf.imageId)
      .leftJoin(ImageTag as it).on(i.id, it.imageId)
      .leftJoin(Tag as t).on(it.tagId, t.id)
      .where(where)
      .groupBy(i.id)

  def apply(rs: WrappedResultSet): Image = {
    val imResult = autoConstruct(rs, i.resultName)
    val gResult = Try{
      Geom.apply(g.resultName)(rs)
    }.toOption
    val files = extractFiles(rs, imResult)
    val tags = extractTags(rs, imResult)
    imResult.toImage(gResult).copy(files = files, tags = tags)
  }

  private def extractFiles(rs: WrappedResultSet, raw: ImageRaw): Seq[ImageFile] = {
    val ids = rs.string("imf_ids").split(',')
    val paths = rs.string("imf_paths").split(',')
    val filesizes = rs.string("imf_filesizes").split(',')
    @nowarn
    val files = (ids :: paths :: filesizes :: Nil).transpose.map { case List(id, path, filesize) =>
      ImageFile(id.toLong, raw.id, path, filesize.toLong)
    }
    files
  }

  private def extractTags(rs: WrappedResultSet, raw: ImageRaw): Seq[Tag] = {
    val ids = rs.stringOpt("t_ids").map(_.split(',')).getOrElse(Array.empty[String]).distinct
    val names = rs.stringOpt("t_names").map(_.split(',')).getOrElse(Array.empty[String]).distinct
    @nowarn
    val tags = (ids :: names :: Nil).transpose.map { case List(id, name) =>
      Tag(id.toLong, name)
    }
    tags
  }

  def find(id: Long)(implicit session: DBSession): Option[Image] = withSQL {
      selectWithJoin(sqls.eq(i.id, id))
    }.map(apply).single.apply()

  def findAllInIds(ids: Seq[Long])(implicit session: DBSession): Seq[Image] = withSQL {
    selectWithJoin(sqls.in(i.id, ids))
  }.map(apply).list.apply()

  def findFromDate(date: LocalDate)(implicit session: DBSession): Seq[Image] = withSQL {
      selectWithJoin(sqls.between(i.shootingAt, date.atStartOfDay(), date.plusDays(1).atStartOfDay()))
          .orderBy(i.shootingAt)
    }.map(apply).list.apply()

  def searchFulltext(
      search: SearchParams,
      paging: Paging = Paging.NoLimit
  )(implicit session: DBSession): Seq[Image] = {
    val result = withSQL {
      selectWithJoin(search.query)
          .limit(paging.limit).offset(paging.offset)
    }.map(apply).list.apply()
    findAllInIds(result.map(_.id))
  }

  def searchFulltextDateCount(search: SearchParams)(implicit session: DBSession): Map[LocalDate, Int] = withSQL {
    val dateCol = sqls"cast(${i.shootingAt} as date)"
    select(dateCol, count(distinct(i.id))).from(Image as i)
        .leftJoin(Geom as g).on(i.geoId, g.id)
        .innerJoin(ImageFile as imf).on(i.id, imf.imageId)
        .where(search.query)
        .groupBy(dateCol)
  }.map { rs =>
    rs.localDate(1) -> rs.int(2)
  }.list.apply().toMap

  def searchFulltextCount(search: SearchParams)(implicit session: DBSession): Long = withSQL {
    select(count(distinct(i.id))).from(Image as i)
        .leftJoin(Geom as g).on(i.geoId, g.id)
        .innerJoin(ImageFile as imf).on(i.id, imf.imageId)
        .where(search.query)
  }.map(_.int(1)).single.apply().get
}
