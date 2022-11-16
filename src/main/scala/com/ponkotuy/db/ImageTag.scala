package com.ponkotuy.db

import scalikejdbc.*

case class ImageTag(imageId: Long, tagId: Long)

object ImageTag extends SQLSyntaxSupport[ImageTag] {
  val it = ImageTag.syntax("it")

  def create(imageId: Long, tagId: Long)(implicit session: DBSession): Unit = applyUpdate {
    insert.into(ImageTag).namedValues(
      column.imageId -> imageId,
      column.tagId -> tagId
    )
  }

  def remove(imageId: Long, tagId: Long)(implicit session: DBSession): Long = applyUpdate {
    delete.from(ImageTag).where.eq(column.imageId, imageId).and.eq(column.tagId, tagId)
  }
}
