package com.ponkotuy.service

import com.ponkotuy.batch.ExifParser
import com.ponkotuy.db.{ Image, ImageFile, ImageWithAll }
import com.ponkotuy.util.Extensions.{ isImageFile, isRawFile }
import scalikejdbc.{ DB, DBSession }

import java.nio.file.Path

class ImageService(photosDir: Path) {
  def findImage(id: Long, isPublic: Boolean, withExif: Boolean): Option[Image] = DB.readOnly { implicit session =>
    ImageWithAll.find(id, isPublic).map { image =>
      if (withExif) { setExif(image) }
      else image
    }
  }

  def setExif(image: Image)(implicit session: DBSession): Image = {
    image.exif.fold[Image] {
      image.copy(exif = Some(ExifCacheService.getOrElseUpdate(image)))
    }(_ => image)
  }

  def imagePath(file: ImageFile): Path = photosDir.resolve(file.path.tail)
}
