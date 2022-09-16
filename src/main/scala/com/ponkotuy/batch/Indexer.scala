package com.ponkotuy.batch

import com.ponkotuy.config.{DBConfig, MyConfig}
import com.ponkotuy.db.{CreateImage, Image, ImageFile}
import com.ponkotuy.geo.Nominatim
import scalikejdbc.*

import java.lang.Thread
import java.nio.file.*
import scala.concurrent.duration.Duration
import scala.jdk.StreamConverters.*
import scala.util.control.NonFatal

class Indexer(conf: MyConfig) extends Runnable {
  val nominatim = new Nominatim()

  override def run(): Unit = {
    val files = Files.walk(conf.app.photosDir).toScala(LazyList)
        .filter(Files.isRegularFile(_))
    files.foreach{ file =>
      val path = conf.app.photosDir.relativize(file).toString
      if(!DB.readOnly(ImageFile.exists(path))) {
        ExifParser.parse(file).foreach { exif =>
          println(file)
          createImageFile(exif, path)
        }
      }
    }
  }

  private def createImageFile(exif: Exif, path: String): Unit = DB localTx { implicit session =>
    try {
      val imageId = Image.find(exif.serialNo, exif.shotId).fold{
        val reverse = exif.latLon.flatMap{ p => nominatim.reverse(p.getLat, p.getLng) }
        reverse.map(_.displayName.reverse.mkString).foreach(println)
        CreateImage(
          exif.serialNo,
          exif.shotId,
          exif.shootingAt,
          reverse.map(_.displayName.reverse.mkString),
          exif.latLon.map(_.getLat),
          exif.latLon.map(_.getLng)
        ).create()
      }{ image => image.id }
      ImageFile.create(imageId, path)
    } catch {
      case NonFatal(e) =>
        e.printStackTrace()
        throw e
    }
  }
}
