package com.ponkotuy.service

import com.ponkotuy.batch.ExifParser
import com.ponkotuy.config.MyConfig
import com.ponkotuy.db.{ Image, ImageFile, ImageWithAll }
import com.ponkotuy.util.Extensions.{ isImageFile, isRawFile }
import scalikejdbc.DB

import java.nio.file.Path

class ImageService(photosDir: Path) {
  def findImage(id: Long, isPublic: Boolean, withExif: Boolean): Option[Image] = DB.readOnly { implicit session =>
    ImageWithAll.find(id, isPublic).map { image =>
      if (withExif) { setExif(image) }
      else image
    }
  }

  def setExif(image: Image): Image = {
    val result = for {
      file <- image.files.find(f => isRawFile(f.path))
        .orElse(image.files.find(f => isImageFile(f.path)))
      detail <- ExifParser.parseDetail(imagePath(file))
    } yield image.copy(exif = Some(detail))
    result.getOrElse(image)
  }

  def imagePath(file: ImageFile): Path = photosDir.resolve(file.path.tail)
}
