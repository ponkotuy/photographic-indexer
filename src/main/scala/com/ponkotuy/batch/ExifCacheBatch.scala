package com.ponkotuy.batch

import com.ponkotuy.config.AppConfig
import com.ponkotuy.db.ExifCache.ec
import com.ponkotuy.db.ImageWithAll
import com.ponkotuy.service.ExifCacheService
import scalikejdbc.*

import scala.util.control.NonFatal

class ExifCacheBatch(appConfig: AppConfig) extends Runnable {
  override def run(): Unit = DB.autoCommit { implicit session =>
    ImageWithAll.findAllIterator(sqls.isNull(ec.imageId)).foreach { images =>
      val count = images.map { image =>
        try {
          ExifCacheService.getOrElseUpdate(image, appConfig.photosDir)
          1
        } catch {
          case NonFatal(e) =>
            println(s"Failed to create EXIF cache for imageId=${ image.id }: ${ e.getMessage }")
            0
        }
      }.sum
      println(s"Create EXIF cache: count=${ count }")
    }
  }
}
