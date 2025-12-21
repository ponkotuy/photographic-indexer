package com.ponkotuy.db

import scalikejdbc.*

import java.time.LocalDateTime
import scala.util.Random

object TestDataGenerator {
  def image(
      id: Long = 0L,
      fileId: Long = 0L,
      cameraId: Int = 2,
      shotId: Long = Random.nextLong(),
      shootingAt: LocalDateTime = LocalDateTime.now(),
      geoId: Option[Long] = None,
      isPublic: Boolean = false,
      note: Option[String] = None
  )(implicit session: DBSession): Long = {
    import Image.column
    val imageId = withSQL {
      insert.into(Image).namedValues(
        column.id -> id,
        column.cameraId -> cameraId,
        column.shotId -> shotId,
        column.shootingAt -> shootingAt,
        column.geoId -> geoId,
        column.isPublic -> isPublic,
        column.note -> note
      )
    }.updateAndReturnGeneratedKey.apply()
    imageFile(imageId = imageId, id = fileId)
    imageId
  }

  def imageFile(
      imageId: Long,
      id: Long = 0L,
      path: String = s"/${ Random.alphanumeric.take(10) }/${ Random.alphanumeric.take(6) }.jpg",
      filesize: Long = 102400L
  )(implicit session: DBSession): Long = {
    import ImageFile.column
    withSQL {
      insert.into(ImageFile).namedValues(
        column.id -> id,
        column.imageId -> imageId,
        column.path -> path,
        column.filesize -> filesize
      )
    }.updateAndReturnGeneratedKey.apply()
  }
}
