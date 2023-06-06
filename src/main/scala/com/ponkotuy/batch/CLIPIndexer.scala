package com.ponkotuy.batch

import com.ponkotuy.clip.ClipAccessor
import com.ponkotuy.config.{AppConfig, ClipConfig}
import com.ponkotuy.db.ImageClipIndex.ici
import scalikejdbc.*
import com.ponkotuy.db.{ImageClipIndex, ImageWithAll}
import com.ponkotuy.util.Extensions

import java.nio.file.Paths

class CLIPIndexer(clipConfig: ClipConfig, appConfig: AppConfig) extends Runnable {
  override def run(): Unit = DB.autoCommit { implicit session =>
    val clip = ClipAccessor(clipConfig)
    ImageWithAll.findAllIterator(sqls.isNull(ici.imageId)).foreach { images =>
      images.foreach { image =>
        for {
          file <- image.files.filter(file => Extensions.isImageFile(file.path)).minByOption(_.filesize)
          path = appConfig.photosDir.resolve(file.path.tail)
          _ = println(s"Calculate CLIP index: ${path}")
          tensor <- clip.image(path)
        } {
          if(ImageClipIndex.create(image.id, tensor) < 1)
            println(s"Insert Error: imageId=${image.id}, idx=${tensor.mkString("Array(", ", ", ")")}")
        }
      }
    }
  }
}
