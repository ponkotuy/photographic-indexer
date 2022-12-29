package com.ponkotuy

import com.ponkotuy.batch.{ExifParser, Initializer}
import com.ponkotuy.config.{FlickrConfig, MyConfig}
import com.ponkotuy.db.{Image, ImageTag, ImageWithAll, Tag}
import com.ponkotuy.flickr.{FlickrAccessor, FlickrPhoto}
import scalikejdbc.DB

import java.io.{File, InputStream}
import java.net.URL
import java.nio.file.{Files, StandardCopyOption}
import scala.util.Using
import scala.util.Using.{Releasable, resource}

object FlickrCrawler {
  def main(args: Array[String]): Unit = {
    val conf = MyConfig.load().getOrElse(throw new RuntimeException("ConfigError"))
    Initializer.initDB(conf.db)
    conf.flickr.foreach(crawlFlickr)
  }

  def crawlFlickr(conf: FlickrConfig): Unit = {
    val flickr = new FlickrAccessor(conf.key, conf.secret)

    Iterator.from(1).map { page =>
      println(page)
      flickr.getPeoplePhotos(conf.me, page)
    }.map { photos =>
      photos.foreach { photo =>
        println(s"${photo.id}: ${photo.title}")
        updateFromFlickr(photos.head)
      }
      Thread.sleep(2000)
      photos.last.id
    }.sliding(2).takeWhile(group => group.head != group(1)).size // sizeは評価を起こすために必要

  }

  def updateFromFlickr(photo: FlickrPhoto): Unit = {
    implicit val releaseFile: Releasable[File] = resource => resource.deleteOnExit()

    DB.localTx { implicit session =>
      val tagMap: Map[String, Long] = Tag.findAll().view.map(t => t.name -> t.id).toMap
      Using(createTemp(photo.originalUrl)) { temp =>
        for {
          exif <- ExifParser.parse(temp.toPath)
          image <- ImageWithAll.find(exif.serialNo, exif.shotId)
        } {
          temp.deleteOnExit()

          val note = photo.description.fold(photo.title)(description => s"${photo.title} - ${description}")
          Image.save(image.id, isPublic = true, note = Some(note))

          val diffTags = photo.tags.toSet -- image.tags.view.map(_.name).toSet
          diffTags.foreach { tag =>
            val tagId = tagMap.getOrElse(tag, Tag.create(tag))
            ImageTag.create(image.id, tagId)
          }
        }
      }
    }
  }

  def createTemp(path: String): File = {
    val file = File.createTempFile("p-indexer-", ".jpg")
    val url = new URL(path)
    val inputStream = url.openStream()
    Files.copy(inputStream, file.toPath, StandardCopyOption.REPLACE_EXISTING)
    file
  }
}
