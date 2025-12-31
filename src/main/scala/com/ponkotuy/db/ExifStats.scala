package com.ponkotuy.db

import com.ponkotuy.res.StatsAggregate
import com.ponkotuy.util.{Granularity, StatsFilter}
import scalikejdbc.*

object ExifStats {
  import com.ponkotuy.db.ExifCache.ec
  import com.ponkotuy.db.ImageTag.it

  private val FocalLengthBuckets =
    Seq(14, 24, 35, 50, 70, 105, 135, 200, 300, 400, 500, 600, 800, 1000, 1200)
  private val IsoBuckets =
    Seq(100, 200, 400, 800, 1600, 3200, 6400, 12800, 25600, 51200)

  private case class BucketRange(
      category: String,
      min: Option[Int],
      max: Option[Int]
  )

  private def buildBucketCase(
      column: SQLSyntax,
      buckets: Seq[Int]
  ): SQLSyntax = {
    val cases = buckets
      .sliding(2)
      .map { case Seq(low, high) =>
        sqls"when $column >= $low and $column < $high then ${s"$low-${high - 1}"}"
      }
      .toSeq :+ sqls"when $column >= ${buckets.last} then ${s"${buckets.last}+"}"

    sqls"case when $column < ${buckets.head} then ${s"<${buckets.head}"} ${sqls.join(cases, sqls" ")} else 'unknown' end"
  }

  private def parseBucketRange(
      category: String,
      buckets: Seq[Int]
  ): BucketRange = {
    if (category.startsWith("<")) {
      BucketRange(category, None, Some(buckets.head - 1))
    } else if (category.endsWith("+")) {
      BucketRange(category, Some(buckets.last), None)
    } else if (category == "unknown") {
      BucketRange(category, None, None)
    } else {
      val parts = category.split("-")
      BucketRange(category, Some(parts(0).toInt), Some(parts(1).toInt))
    }
  }

  private def buildFilterCondition(filter: StatsFilter): Option[SQLSyntax] = {
    val conditions = Seq(
      filter.camera.map(c => sqls"${ec.camera} = $c"),
      filter.lens.map(l => sqls"${ec.lens} = $l")
    ).flatten

    if (conditions.isEmpty) None
    else Some(sqls.join(conditions, sqls" and "))
  }

  private def buildTagSubquery(tagId: Long): SQLSyntax = {
    sqls"${ec.imageId} in (select ${it.imageId} from ${ImageTag as it} where ${it.tagId} = $tagId)"
  }

  private def combineConditions(conditions: Option[SQLSyntax]*): SQLSyntax = {
    val nonEmpty = conditions.flatten
    if (nonEmpty.isEmpty) sqls"true"
    else sqls.join(nonEmpty, sqls" and ")
  }

  def aggregateByFocalLength(
      granularity: Granularity,
      filter: StatsFilter = StatsFilter()
  )(implicit session: DBSession): Seq[StatsAggregate] = {
    val dateSql = sqls"date_format(${ec.shootingAt}, ${granularity.dateFormat})"
    val bucketSql = buildBucketCase(ec.focalLength, FocalLengthBuckets)
    val baseCond = sqls"${ec.focalLength} is not null"
    val filterCond = buildFilterCondition(filter)
    val tagCond = filter.tagId.map(buildTagSubquery)
    val granularityCond = granularity.condition(ec.shootingAt)
    val cond =
      combineConditions(Some(baseCond), filterCond, tagCond, granularityCond)

    withSQL {
      select(dateSql, bucketSql, sqls.count)
        .from(ExifCache as ec)
        .where(cond)
        .groupBy(dateSql, bucketSql)
        .orderBy(dateSql)
    }.map { rs =>
      val range = parseBucketRange(rs.string(2), FocalLengthBuckets)
      StatsAggregate(
        rs.string(1),
        range.category,
        range.min,
        range.max,
        rs.int(3)
      )
    }.list
      .apply()
  }

  def aggregateByCamera(
      granularity: Granularity,
      filter: StatsFilter = StatsFilter()
  )(implicit session: DBSession): Seq[StatsAggregate] = {
    val dateSql = sqls"date_format(${ec.shootingAt}, ${granularity.dateFormat})"
    val filterCond = buildFilterCondition(filter)
    val tagCond = filter.tagId.map(buildTagSubquery)
    val granularityCond = granularity.condition(ec.shootingAt)
    val cond = combineConditions(filterCond, tagCond, granularityCond)

    withSQL {
      select(dateSql, ec.camera, sqls.count)
        .from(ExifCache as ec)
        .where(cond)
        .groupBy(dateSql, ec.camera)
        .orderBy(dateSql)
    }.map(rs =>
      StatsAggregate(rs.string(1), rs.string(2), None, None, rs.int(3))
    ).list
      .apply()
  }

  def aggregateByLens(
      granularity: Granularity,
      filter: StatsFilter = StatsFilter()
  )(implicit session: DBSession): Seq[StatsAggregate] = {
    val dateSql = sqls"date_format(${ec.shootingAt}, ${granularity.dateFormat})"
    val baseCond = sqls"${ec.lens} is not null"
    val filterCond = buildFilterCondition(filter)
    val tagCond = filter.tagId.map(buildTagSubquery)
    val granularityCond = granularity.condition(ec.shootingAt)
    val cond =
      combineConditions(Some(baseCond), filterCond, tagCond, granularityCond)

    withSQL {
      select(dateSql, ec.lens, sqls.count)
        .from(ExifCache as ec)
        .where(cond)
        .groupBy(dateSql, ec.lens)
        .orderBy(dateSql)
    }.map(rs =>
      StatsAggregate(rs.string(1), rs.string(2), None, None, rs.int(3))
    ).list
      .apply()
  }

  def aggregateByIso(
      granularity: Granularity,
      filter: StatsFilter = StatsFilter()
  )(implicit session: DBSession): Seq[StatsAggregate] = {
    val dateSql = sqls"date_format(${ec.shootingAt}, ${granularity.dateFormat})"
    val bucketSql = buildBucketCase(ec.iso, IsoBuckets)
    val baseCond = sqls"${ec.iso} is not null"
    val filterCond = buildFilterCondition(filter)
    val tagCond = filter.tagId.map(buildTagSubquery)
    val granularityCond = granularity.condition(ec.shootingAt)
    val cond =
      combineConditions(Some(baseCond), filterCond, tagCond, granularityCond)

    withSQL {
      select(dateSql, bucketSql, sqls.count)
        .from(ExifCache as ec)
        .where(cond)
        .groupBy(dateSql, bucketSql)
        .orderBy(dateSql)
    }.map { rs =>
      val range = parseBucketRange(rs.string(2), IsoBuckets)
      StatsAggregate(
        rs.string(1),
        range.category,
        range.min,
        range.max,
        rs.int(3)
      )
    }.list
      .apply()
  }

  def listCameras()(implicit session: DBSession): Seq[String] = {
    withSQL {
      select(sqls.distinct(ec.camera))
        .from(ExifCache as ec)
        .orderBy(ec.camera)
    }.map(_.string(1)).list.apply()
  }

  def listLenses()(implicit session: DBSession): Seq[String] = {
    withSQL {
      select(sqls.distinct(ec.lens))
        .from(ExifCache as ec)
        .where
        .isNotNull(ec.lens)
        .orderBy(ec.lens)
    }.map(_.string(1)).list.apply()
  }
}
