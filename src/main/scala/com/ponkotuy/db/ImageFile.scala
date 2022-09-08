package com.ponkotuy.db

import java.time.LocalDateTime
import scalikejdbc._


case class ImageFile(id: Long, image: Image, path: String)
case class ImageFileRaw(id: Long, imageId: Long, path: String)

object ImageFile extends SQLSyntaxSupport[ImageFileRaw] {
  val imf = ImageFile.syntax("imf")

  def apply(rn: ResultName[ImageFileRaw])(rs: WrappedResultSet): ImageFileRaw =
    autoConstruct(rs, rn)

  def create(imageId: Long, path: String)(implicit session: DBSession): Long = {
    withSQL {
      insert.into(ImageFile)
          .namedValues(column.imageId -> imageId, column.path -> path)
    }.updateAndReturnGeneratedKey.apply()
  }

  def exists(path: String)(implicit session: DBSession): Boolean = {
    withSQL {
      select.from(ImageFile as imf).where.eq(imf.path, path)
    }.map(ImageFile(imf.resultName)).single.apply().isDefined
  }
}
