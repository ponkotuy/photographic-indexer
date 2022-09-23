package com.ponkotuy.batch

import com.ponkotuy.config.{DBConfig, MyConfig}
import com.ponkotuy.db.{CreateImage, Image, ImageFile}
import com.ponkotuy.geo.Nominatim
import scalikejdbc.*

import java.lang.Thread
import java.nio.file.*
import java.util.Locale
import scala.concurrent.duration.Duration
import scala.jdk.StreamConverters.*
import scala.util.control.NonFatal

class Indexer(conf: MyConfig) extends Runnable {
  val nominatim = new Nominatim()

  override def run(): Unit = {
    val files = Files.walk(conf.app.photosDir).toScala(LazyList)
        .filter(Files.isRegularFile(_))
    files.foreach { file =>
      val path = ImagePath(file, conf.app.photosDir.relativize(file))
      if(!DB.readOnly(ImageFile.exists(path.name))) {
        if(Extensions.isRetouchFile(path.lastname)) createRetouchFile(path)
        else {
          ExifParser.parse(file).foreach{ exif =>
            println(file)
            createImageFile(exif, path)
          }
        }
      }
    }
  }

  private def createImageFile(exif: Exif, path: ImagePath): Unit = DB localTx { implicit session =>
    try {
      val imageId = Image.find(exif.serialNo, exif.shotId).fold{
        val reverse = exif.latLon.flatMap { p => nominatim.reverse(p.getLat, p.getLng) }
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
      ImageFile.create(imageId, path.name, Files.size(path.absolute))
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

object Extensions {
  val Images: Seq[String] = "jpeg" :: "jpg" :: "tiff" :: "tif" :: "png" :: "bmp" :: "webp" :: "gif" :: Nil
  val Raws: Seq[String] =
    "cr2" :: "cr3" :: "crw" :: // Canon
        "raf" :: // FUJIFILM
        "rwl" ::  // Leica
        "nef" :: "nrw" :: // Nikon
        "orf" :: // OM
        "rw2" :: // Panasonic
        "pef" :: // PENTAX
        "x3f" :: // SIGMA
        "arw" :: "sr2" :: "srf" ::// SONY
        "dng" :: Nil // Common
  val Retouches: Seq[String] =
    "xmp" :: // Darktable
    "dop" :: // DxO Photolab
    "dr4" :: // Digital Photo Professional
    "nksc" :: // Nikon Capture NX-D
        Nil

  def isRetouchFile(str: String): Boolean = {
    val xs = str.split('.')
    2 < xs.length &&
        ((Raws ++ Images).contains(toLower(xs(1))) || Retouches.contains(toLower(xs.last)))
  }

  private def toLower(str: String) = str.toLowerCase(Locale.ENGLISH)

  def retouchOrigin(lastname: String): String = {
    val xs = lastname.split('.')
    require(2 < xs.length, s"${lastname} is not retouch filename")
    xs.init.mkString(".")
  }
}
