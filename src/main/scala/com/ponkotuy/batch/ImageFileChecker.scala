package com.ponkotuy.batch

import com.ponkotuy.config.AppConfig
import com.ponkotuy.db.{ Image, ImageFile, ImageWithAll }
import scalikejdbc.*

import java.nio.file.Files

class ImageFileChecker(conf: AppConfig) extends Runnable {
  override def run(): Unit = {
    removeNotExistsImageFiles()
    removeNotExistsFileImages()
    println("Done ImageFileChecker::run")
  }

  private def removeNotExistsImageFiles(): Unit = {
    import ImageFile.imf
    DB.autoCommit { implicit session =>
      var lastId = 0L
      while {
        val records = ImageFile.findAll(sqls.gt(imf.id, lastId), limit = 200)
        records.foreach { record =>
          val path = conf.photosDir.resolve(record.path.tail)
          if (!Files.exists(path)) {
            println(s"Delete record: id=${ record.id } path=${ record.path }")
            ImageFile.remove(record.id)
          }
        }
        records.lastOption.foreach(r => lastId = r.id)
        records.nonEmpty
      } do ()
    }
  }

  private def removeNotExistsFileImages(): Unit = {
    DB.autoCommit { implicit session =>
      ImageWithAll.findAllIterator().foreach { records =>
        records.foreach { record =>
          if (record.files.isEmpty) {
            println(s"Delete no image file record: id=${ record.id }")
            Image.remove(record.id)
          }
        }
      }
    }
  }
}
