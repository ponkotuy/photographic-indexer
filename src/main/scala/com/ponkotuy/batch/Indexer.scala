package com.ponkotuy.batch

import com.ponkotuy.config.AppConfig
import com.ponkotuy.db.{ CreateImage, Image, ImageFile }
import com.ponkotuy.geo.Nominatim
import com.ponkotuy.service.ExifCacheService
import com.ponkotuy.util.Extensions
import scalikejdbc.*

import java.nio.file.*
import scala.jdk.StreamConverters.*
import scala.util.control.NonFatal

class Indexer(conf: AppConfig) extends Runnable {
  private val nominatim = new Nominatim()

  override def run(): Unit = {
    val files = Files.walk(conf.photosDir).toScala(LazyList)
      .filter(Files.isRegularFile(_))
      .filter { file => Extensions.isTarget(file.toString) }
    files.foreach { file =>
      val path = ImagePath(file, conf.photosDir.relativize(file))
      if (!DB.readOnly(ImageFile.exists(path.name))) {
        println(file)
        if (Extensions.isRetouchFile(path.lastname)) createRetouchFile(path)
        else {
          ExifParser.parse(file).foreach { exif =>
            createImageFile(exif, path)
          }
        }
      }
    }
  }

  private def createImageFile(exif: Exif, path: ImagePath): Unit = DB localTx { implicit session =>
    try {
      val imageId = Image.find(exif.cameraId, exif.calcShotId).fold {
        val reverse = exif.latLon.flatMap { p => nominatim.reverse(p.getLat, p.getLng) }
        reverse.map(_.displayName.reverse.mkString).foreach(println)
        CreateImage(
          exif.cameraId,
          exif.calcShotId,
          exif.shootingAt,
          reverse.map(_.displayName.reverse.mkString),
          exif.latLon.map(_.getLat),
          exif.latLon.map(_.getLng)
        ).create()
      } { image =>
        if (exif.shootingAt.isBefore(image.shootingAt)) Image.updateShootingAt(image.id, exif.shootingAt)
        image.id
      }
      ImageFile.create(imageId, path.name, Files.size(path.absolute))
      ExifCacheService.getOrElseUpdate(imageId, path.absolute)
    } catch {
      case NonFatal(e) =>
        e.printStackTrace()
        throw e
    }
  }

  private def createRetouchFile(path: ImagePath): Unit = DB localTx { implicit session =>
    try {
      val origin = Extensions.retouchOrigin(path.name)
      ImageFile.findFromPath(origin).foreach { imageFile =>
        println(path.name)
        ImageFile.create(imageFile.imageId, path.name, Files.size(path.absolute))
      }
    } catch {
      case NonFatal(e) =>
        e.printStackTrace()
        throw e
    }
  }
}

case class ImagePath(absolute: Path, relative: Path) {
  lazy val name: String = "/" + relative.toString
  lazy val lastname: String = relative.getFileName.toString
}
