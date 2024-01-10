package com.ponkotuy

import com.ponkotuy.batch.{ ExifParser, Initializer }
import com.ponkotuy.config.{ FlickrConfig, MyConfig }
import com.ponkotuy.db.{ Image, ImageTag, ImageWithAll, Tag }
import com.ponkotuy.flickr.{ FlickrAccessor, FlickrPhoto }
import scalikejdbc.DB

import java.io.{ File, InputStream }
import java.net.{ URI, URL }
import java.nio.file.{ Files, Path, Paths, StandardCopyOption, StandardOpenOption }
import scala.util.Using
import scala.util.Using.{ Releasable, resource }

object FlickrCrawler {
  def main(args: Array[String]): Unit = {
    val conf = MyConfig.load().getOrElse(throw new RuntimeException("ConfigError"))
    Initializer.initDB(conf.db)
    conf.flickr.foreach(crawlFlickr)
  }

  def oneFlickr(conf: FlickrConfig): Unit = {
    val flickr = new FlickrAccessor(conf.key, conf.secret)
    println("----- EXEC FLICKR TEST -----")
    val photos = flickr.getPeoplePhotos(conf.me, 1, 20)
    photos.foreach(println)
    updateFromFlickr(photos.head)
  }

  def crawlFlickr(conf: FlickrConfig): Unit = {
    val flickr = new FlickrAccessor(conf.key, conf.secret)

    Iterator.from(1).map { page =>
      println(s"Read page=${page}")
      flickr.getPeoplePhotos(conf.me, page, 500)
    }.map { photos =>
      photos.foreach { photo =>
        println(s"${photo.id}: ${photo.title}")
        updateFromFlickr(photo)
      }
      Thread.sleep(2000)
      photos.last.id
    }.sliding(2).takeWhile(group => group.head != group(1)).size // sizeは評価を起こすために必要
  }

  def updateFromFlickr(photo: FlickrPhoto): Unit = {
    implicit val releasePath: Releasable[Path] = resource => Files.delete(resource)

    DB.localTx { implicit session =>
      val tagMap: Map[String, Long] = Tag.findAll().view.map(t => t.name -> t.id).toMap
      for {
        exifResult <- Using(downloadTemp(photo.originalUrl))(ExifParser.parse)
        exif <- exifResult
        image <- ImageWithAll.find(exif.cameraId, exif.calcShotId)
      } {
        val note = photo.description.fold(photo.title)(description => s"${photo.title} - ${description}")
        Image.save(image.id, isPublic = photo.isPublic, note = Some(note))
        println(s"  Add note: '${note}' public: ${photo.isPublic}")

        val flickrTags = photo.tags :+ "flickr"
        val diffTags = flickrTags.toSet -- image.tags.view.map(_.name).toSet
        diffTags.foreach { tag =>
          val tagId = tagMap.getOrElse(tag, Tag.create(tag))
          ImageTag.create(image.id, tagId)
        }
        if(diffTags.nonEmpty) println(s"  Add tags ${diffTags.mkString(",")}")
        else println("  Not exists a additional tag")
      }
    }
  }

  def downloadTemp(path: String): Path = {
    val file = Files.createTempFile("p-indexer-", ".jpg")
    val url = URI.create(path).toURL
    Using(url.openStream()) { is =>
      Files.copy(is, file, StandardCopyOption.REPLACE_EXISTING)
    }
    file
  }
}
