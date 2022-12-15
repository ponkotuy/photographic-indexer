package com.ponkotuy.batch

import com.drew.imaging.ImageMetadataReader
import com.drew.metadata.Tag
import org.gbif.common.parsers.geospatial.{CoordinateParseUtils, LatLng}
import org.apache.commons.math3.fraction.Fraction

import java.nio.file.Path
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.jdk.CollectionConverters.*
import scala.util.Try

object ExifParser {
  import ExifParser._

  private def getTags(file: Path) = {
    Try(ImageMetadataReader.readMetadata(file.toFile)).map {
      _.getDirectories.asScala.flatMap(_.getTags.asScala)
    }.toOption
  }

  private def find(tags: Iterable[Tag], name: String): Option[Tag] =
    tags.find(_.getTagName == name)

  private val NumberPattern = "([\\d.-]+)".r
  private def parseLength(str: String): Int = {
    NumberPattern.findFirstIn(str).get.toInt
  }

  private def parseExposure(str: String): Fraction = {
    val xs = NumberPattern.findAllIn(str).toIndexedSeq
    if(xs.length == 1) {
      new Fraction(xs.head.toDouble)
    } else {
      new Fraction(
        xs.head.toInt,
        xs(1).toInt
      )
    }
  }

  private val Formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss")
  private def timeParser(str: String): LocalDateTime = LocalDateTime.parse(str, Formatter)

  def parse(file: Path): Option[Exif] = {
    for {
      tags <- getTags(file)
      serialNoRaw <- find(tags, "Body Serial Number")
      serialNo <- serialNoRaw.getDescription.toIntOption
      shotIdRaw <- find(tags, "Exposure Sequence Number")
      shotId <- shotIdRaw.getDescription.toIntOption
      shootingAtRaw <- find(tags, "Date/Time")
      shootingAt = timeParser(shootingAtRaw.getDescription)
    } yield {
      val latLon = for {
        latRaw <- find(tags, "GPS Latitude")
        lonRaw <- find(tags, "GPS Longitude")
        latLon = CoordinateParseUtils.parseLatLng(latRaw.getDescription, lonRaw.getDescription).getPayload
      } yield latLon
      Exif(serialNo, shotId, shootingAt, latLon)
    }
  }

  def parseDetail(file: Path): Option[ExifDetail] = {
    for {
      tags <- getTags(file)
      camera = find(tags, "Model").map(_.getDescription).getOrElse("Unknown")
      focal = find(tags, "Focal Length 35")
          .map(tag => parseLength(tag.getDescription))
          .filterNot(_ == 0)
      lens = find(tags, "Lens")
          .orElse(find(tags, "LensModel"))
          .map(_.getDescription)
          .filterNot(_ == "Unknown")
      aperture = for {
        tag <- find(tags, "F-Number")
        fNumber <- NumberPattern.findFirstIn(tag.getDescription)
      } yield BigDecimal(fNumber)
      exposure <- find(tags, "Exposure Time").map(tag => parseExposure(tag.getDescription))
      iso <- find(tags, "ISO Speed Ratings").map(tag => tag.getDescription.toInt)
    } yield ExifDetail(camera, lens, focal, aperture, exposure, iso)
  }

}

case class Exif(serialNo: Int, shotId: Int, shootingAt: LocalDateTime, latLon: Option[LatLng])

case class ExifDetail(
    camera: String,
    lens: Option[String],
    focal: Option[Int],
    aperture: Option[BigDecimal],
    exposureTime: Fraction,
    iso: Int
)
