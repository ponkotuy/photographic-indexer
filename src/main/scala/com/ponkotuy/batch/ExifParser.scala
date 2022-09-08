package com.ponkotuy.batch

import com.drew.imaging.ImageMetadataReader
import org.gbif.common.parsers.geospatial.{CoordinateParseUtils, LatLng}

import java.nio.file.Path
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.jdk.CollectionConverters.*
import scala.util.Try

object ExifParser {
  import ExifParser._

  def parse(file: Path): Option[Exif] = {
    val metadata = Try(ImageMetadataReader.readMetadata(file.toFile)).getOrElse(return None)
    val tags = metadata.getDirectories.asScala.flatMap(_.getTags.asScala)
    for {
      serialNoRaw <- tags.find(_.getTagName == "Body Serial Number")
      serialNo <- serialNoRaw.getDescription.toIntOption
      shotIdRaw <- tags.find(_.getTagName == "Exposure Sequence Number")
      shotId <- shotIdRaw.getDescription.toIntOption
      shootingAtRaw <- tags.find(_.getTagName == "Date/Time")
      shootingAt = timeParser(shootingAtRaw.getDescription)
    } yield {
      val latLon = for {
        latRaw <- tags.find(_.getTagName == "GPS Latitude")
        lonRaw <- tags.find(_.getTagName == "GPS Longitude")
        latLon = CoordinateParseUtils.parseLatLng(latRaw.getDescription, lonRaw.getDescription).getPayload
      } yield latLon
      Exif(serialNo, shotId, shootingAt, latLon)
    }
  }

  val Formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss")
  def timeParser(str: String): LocalDateTime = LocalDateTime.parse(str, Formatter)
}

case class Exif(serialNo: Int, shotId: Int, shootingAt: LocalDateTime, latLon: Option[LatLng])
