package com.ponkotuy.db

import com.ponkotuy.util.Extensions

import java.time.LocalDateTime
import scalikejdbc.*

case class ImageFile(id: Long, imageId: Long, path: String, filesize: Long) {
  def isRetouch: Boolean = Extensions.isRetouchFile(path)
}

object ImageFile extends SQLSyntaxSupport[ImageFile] {
  val imf = ImageFile.syntax("imf")

  def apply(rn: ResultName[ImageFile])(rs: WrappedResultSet): ImageFile =
    autoConstruct(rs, rn)

  def create(imageId: Long, path: String, filesize: Long)(implicit session: DBSession): Long = {
    withSQL {
      insert.into(ImageFile)
          .namedValues(column.imageId -> imageId, column.path -> path, column.filesize -> filesize)
    }.updateAndReturnGeneratedKey.apply()
  }

  def exists(path: String)(implicit session: DBSession): Boolean = findFromPath(path).isDefined

  def findFromPath(path: String)(implicit session: DBSession): Option[ImageFile] = withSQL {
      select.from(ImageFile as imf).where.eq(imf.path, path)
    }.map(ImageFile(imf.resultName)).single.apply()

  def findAllInImageIds(imageIds: Seq[Long])(implicit session: DBSession): List[ImageFile] = withSQL {
    select.from(ImageFile as imf).where.in(imf.imageId, imageIds)
  }.map(ImageFile(imf.resultName)).list.apply()
}
