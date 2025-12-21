package com.ponkotuy.db

import scalikejdbc._
import io.circe.syntax._

case class ImageClipIndex(imageId: Long, clipIndex: Array[Float])

object ImageClipIndex extends SQLSyntaxSupport[ImageClipIndex] {
  val ici = ImageClipIndex.syntax("ici")

  def apply(rn: ResultName[ImageClipIndex])(rs: WrappedResultSet): ImageClipIndex = {
    import io.circe._
    import io.circe.parser._

    val imageId = rs.long(rn.imageId)
    val clipIndexRaw = rs.string(rn.clipIndex)
    val json = parse(clipIndexRaw).fold(throw _, identity)
    val clipIndex = json.as[Array[Float]].fold(throw _, identity)
    new ImageClipIndex(imageId, clipIndex)
  }

  def count(where: SQLSyntax = sqls"true")(implicit session: DBSession): Option[Int] = withSQL {
    select(sqls.count).from(ImageClipIndex as ici).where(where)
  }.map(rs => rs.int(1)).single.apply()

  def findAll(
      where: SQLSyntax = sqls"true",
      limit: Int = Int.MaxValue,
      offset: Int = 0
  )(implicit session: DBSession): Seq[ImageClipIndex] = withSQL {
    select.from(ImageClipIndex as ici).where(where).limit(limit).offset(offset)
  }.map(ImageClipIndex(ici.resultName)).list.apply()

  def create(imageId: Long, clipIndex: Array[Float])(implicit session: DBSession): Int = applyUpdate {
    insert.into(ImageClipIndex)
      .namedValues(
        column.imageId -> imageId,
        column.clipIndex -> clipIndex.asJson.toString
      )
  }
}
