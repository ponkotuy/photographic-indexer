package com.ponkotuy.batch

import com.ponkotuy.config.{AppConfig, MyConfig}
import com.ponkotuy.db.ImageFile
import scalikejdbc.{DB, sqls}

import java.nio.file.{Files, Paths}

class ImageFileChecker(conf: AppConfig) extends Runnable {
  override def run(): Unit = {
    import ImageFile.imf

    DB readOnly { implicit session =>
      var lastId = 0L
      while {
        val records = ImageFile.findAll(sqls.gt(imf.id, lastId), limit = 200)
        records.foreach { record =>
          val path = conf.photosDir.resolve(record.path.tail)
          if(!Files.exists(path)) {
            println(s"Delete record: id=${record.id} path=${record.path}")
          }
        }
        records.lastOption.foreach(r => lastId = r.id)
        records.nonEmpty
      } do ()
    }
    println("Done ImageFileChecker::run")
  }
}
