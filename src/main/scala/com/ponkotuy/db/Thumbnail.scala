package com.ponkotuy.db

import scalikejdbc.*

import java.io.{ByteArrayInputStream, InputStream}
import java.time.LocalDateTime

case class Thumbnail(imageId: Long, file: Array[Byte], createdAt: LocalDateTime)

object Thumbnail extends SQLSyntaxSupport[Thumbnail] {
  val t = Thumbnail.syntax("t")

  def apply(rn: ResultName[Thumbnail])(rs: WrappedResultSet): Thumbnail =
    autoConstruct(rs, rn)

  def find(imageId: Long)(implicit session: DBSession): Option[Thumbnail] = withSQL {
    select.from(Thumbnail as t).where.eq(t.imageId, imageId)
  }.map(Thumbnail(t.resultName)).single.apply()

  def create(
      imageId: Long,
      file: Array[Byte],
      createdAt: LocalDateTime = LocalDateTime.now()
  )(implicit session: DBSession): Unit = applyUpdate {
    val bais: InputStream = ByteArrayInputStream(file)
    insert.into(Thumbnail).namedValues(
      column.imageId -> imageId,
      column.file -> bais,
      column.createdAt -> createdAt
    )
  }
}
