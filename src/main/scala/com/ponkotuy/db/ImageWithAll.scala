package com.ponkotuy.db

import com.ponkotuy.req.SearchParams
import com.ponkotuy.res.Paging
import scalikejdbc.*
import scalikejdbc.sqls.{ count, distinct }

import java.time.{ LocalDate, YearMonth }
import scala.annotation.nowarn
import scala.util.Try

object ImageWithAll {
  import ExifCache.ec
  import FlickrImage.fi
  import Geom.g
  import Image.i
  import ImageClipIndex.ici
  import ImageFile.imf
  import ImageTag.it
  import Tag.t

  private def groupConcat(sql: SQLSyntax, name: SQLSyntax) = sqls"group_concat(${ sql }) as ${ name }"
  private val imfSelect = groupConcat(imf.id, sqls"imf_ids") ::
    groupConcat(imf.path, sqls"imf_paths") ::
    groupConcat(imf.filesize, sqls"imf_filesizes") :: Nil
  private val tSelect = groupConcat(t.id, sqls"t_ids") ::
    groupConcat(t.name, sqls"t_names") :: Nil
  private val selectAll = i.resultAll +: fi.resultAll +: ec.resultAll +: (imfSelect ++ tSelect ++ Geom.select)
  private def selectWithJoin(where: SQLSyntax) = select(selectAll*).from(Image as i)
    .leftJoin(Geom as g).on(i.geoId, g.id)
    .innerJoin(ImageFile as imf).on(i.id, imf.imageId)
    .leftJoin(ImageTag as it).on(i.id, it.imageId)
    .leftJoin(Tag as t).on(it.tagId, t.id)
    .leftJoin(ImageClipIndex as ici).on(i.id, ici.imageId)
    .leftJoin(FlickrImage as fi).on(i.id, fi.imageId)
    .leftJoin(ExifCache as ec).on(i.id, ec.imageId)
    .where(where)
    .groupBy(i.id)

  val isPublicSQL: SQLSyntax = sqls.eq(i.isPublic, true)

  def apply(rs: WrappedResultSet): Image = {
    val imResult = autoConstruct(rs, i.resultName)
    val gResult = Try { Geom.apply(g.resultName)(rs) }.toOption
    val fResult = Try { FlickrImage.apply(fi.resultName)(rs) }.toOption
    val files = extractFiles(rs, imResult)
    val tags = extractTags(rs)
    val clip = Try { ImageClipIndex.apply(ici.resultName)(rs) }.toOption
    val exif = Try { ExifCache.apply(ec.resultName)(rs) }.toOption
    imResult.toImage(geo = gResult, files = files, tags = tags, exif = exif, clipIndex = clip, flickr = fResult)
  }

  private def extractFiles(rs: WrappedResultSet, raw: ImageRaw): Seq[ImageFile] = {
    val ids = rs.string("imf_ids").split(',')
    val paths = rs.string("imf_paths").split(',')
    val fileSizes = rs.string("imf_filesizes").split(',')
    @nowarn
    val files = (ids :: paths :: fileSizes :: Nil).transpose.map { case List(id, path, filesize) =>
      ImageFile(id.toLong, raw.id, path, filesize.toLong)
    }
    files.distinct.sortBy(_.path)
  }

  private def extractTags(rs: WrappedResultSet): Seq[Tag] = {
    val ids = rs.stringOpt("t_ids").map(_.split(',')).getOrElse(Array.empty[String]).distinct
    val names = rs.stringOpt("t_names").map(_.split(',')).getOrElse(Array.empty[String]).distinct
    @nowarn
    val tags = (ids :: names :: Nil).transpose.map { case List(id, name) =>
      Tag(id.toLong, name)
    }
    tags.distinct.sortBy(_.name)
  }

  def find(id: Long, isPublic: Boolean)(implicit session: DBSession): Option[Image] = withSQL {
    selectWithJoin(sqls.eq(i.id, id).and(if (isPublic) Some(isPublicSQL) else None))
  }.map(apply).single.apply()

  def find(cameraId: Int, shotId: Long)(implicit session: DBSession): Option[Image] = withSQL {
    selectWithJoin(sqls.eq(i.cameraId, cameraId).and.eq(i.shotId, shotId))
  }.map(apply).single.apply()

  def findAll(where: SQLSyntax, limit: Int = Int.MaxValue, offset: Int = 0, orderBy: SQLSyntax = i.shootingAt)(implicit
      session: DBSession): Seq[Image] = withSQL {
    selectWithJoin(where).orderBy(orderBy).limit(limit).offset(offset)
  }.map(apply).list.apply()

  def findAllIterator(where: SQLSyntax = sqls"true", grouping: Int = 500)(implicit
      session: DBSession): Iterator[Seq[Image]] = {
    Iterator.unfold(Long.MinValue) { minId =>
      val list = withSQL {
        selectWithJoin(where.and(sqls.gt(i.id, minId))).orderBy(i.id).limit(grouping)
      }.map(apply).list.apply()
      if (list.isEmpty) None else Some(list -> list.last.id)
    }
  }

  def findAllInIds(ids: Seq[Long])(implicit session: DBSession): Seq[Image] = withSQL {
    selectWithJoin(sqls.in(i.id, ids))
  }.map(apply).list.apply()

  def aDay(date: LocalDate): SQLSyntax = {
    val start = date.atStartOfDay()
    val end = start.plusDays(1)
    sqls.between(i.shootingAt, start, end)
  }

  def findRandom(where: SQLSyntax)(implicit session: DBSession): Option[Image] = withSQL {
    selectWithJoin(where).orderBy(sqls"rand()").limit(1)
  }.map(apply).single.apply()

  def aggregateMonthlyByDate(month: YearMonth)(implicit session: DBSession): Map[LocalDate, Seq[Image]] = {
    val start = month.atDay(1).atStartOfDay()
    val end = start.plusMonths(1)
    val images: List[Image] = withSQL {
      selectWithJoin(sqls.between(i.shootingAt, start, end)).orderBy(i.shootingAt)
    }.map(apply).list.apply()
    images.groupBy(_.shootingAt.toLocalDate)
  }

  def searchFulltext(
      search: SearchParams,
      paging: Paging = Paging.NoLimit
  )(implicit session: DBSession): Seq[Image] = {
    val result = withSQL {
      selectWithJoin(search.query)
        .orderBy(search.orderColumns*)
        .limit(paging.limit).offset(paging.offset)
    }.map(apply).list.apply()
    findAllInIds(result.map(_.id))
  }

  def searchFulltextDateCount(search: SearchParams)(implicit session: DBSession): Map[LocalDate, Int] = withSQL {
    val dateCol = sqls"cast(${ i.shootingAt } as date)"
    select(dateCol, count(distinct(i.id))).from(Image as i)
      .leftJoin(Geom as g).on(i.geoId, g.id)
      .innerJoin(ImageFile as imf).on(i.id, imf.imageId)
      .leftJoin(ImageTag as it).on(i.id, it.imageId)
      .leftJoin(Tag as t).on(it.tagId, t.id)
      .where(search.query)
      .groupBy(dateCol)
  }.map { rs =>
    rs.localDate(1) -> rs.int(2)
  }.list.apply().toMap

  def searchFulltextCount(search: SearchParams)(implicit session: DBSession): Long = withSQL {
    select(count(distinct(i.id))).from(Image as i)
      .leftJoin(Geom as g).on(i.geoId, g.id)
      .innerJoin(ImageFile as imf).on(i.id, imf.imageId)
      .leftJoin(ImageTag as it).on(i.id, it.imageId)
      .leftJoin(Tag as t).on(it.tagId, t.id)
      .where(search.query)
  }.map(_.int(1)).single.apply().get

  def findAllCount(where: SQLSyntax)(implicit session: DBSession): Long = withSQL {
    select(count(distinct(i.id))).from(Image as i).where(where)
  }.map(_.int(1)).single.apply().get
}
