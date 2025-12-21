package com.ponkotuy.batch

import io.circe.Decoder
import org.gbif.common.parsers.geospatial.{ CoordinateParseUtils, LatLng }
import org.apache.commons.math3.fraction.Fraction

import java.nio.file.Path
import java.time.{ LocalDateTime, ZoneOffset }
import java.time.format.DateTimeFormatter
import scala.jdk.CollectionConverters.*
import scala.util.Try

object ExifParser {
  private def getTags(file: Path): Option[Exiftool] = {
    Exiftool.run(file).headOption
  }

  def find[T](tags: Exiftool, names: String*)(implicit decoder: Decoder[T]): Option[T] = {
    names.collectFirst {
      case name if tags.contains(name) => tags.get[T](name)
    }.flatten
  }

  private def parseModel(tags: Exiftool): String =
    find(tags, "Model").getOrElse("Unknown")

  private val NumberPattern = "([\\d.-]+)".r
  private def parseLength(str: String): Int = {
    NumberPattern.findAllIn(str).iterator.toSeq.lastOption
      .map(_.toDouble).map(Math.round).map(_.toInt)
      .getOrElse(0)
  }

  private def parseExposure(str: String): Fraction = {
    val xs = NumberPattern.findAllIn(str).toIndexedSeq
    if (xs.length == 1) {
      new Fraction(xs.head.toDouble)
    } else {
      new Fraction(
        xs.head.toInt,
        xs(1).toInt
      )
    }
  }

  private val Formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss[Z]")
  private def timeParser(str: String): Option[LocalDateTime] = Try { LocalDateTime.parse(str, Formatter) }.toOption

  private def parseSerialNumber(tags: Exiftool): Option[Int] = {
    tags.get[Int]("SerialNumber")
      .orElse(tags.get[String]("SerialNumber").map { sn =>
        sn.toLongOption.map(_.toInt).getOrElse(sn.hashCode)
      }).orElse(
        tags.get[String]("InternalSerialNumber")
          .filter(_.nonEmpty)
          .flatMap { str =>
            Try(java.lang.Long.parseLong(str, 16).toInt).toOption
              .orElse(NumberPattern.findFirstIn(str).map(_.toInt))
          }
      )
  }

  private def parseLatLon(tags: Exiftool): Option[LatLng] = {
    def format(str: String): String = str.replace(" deg ", " ")
    for {
      latRaw <- find[String](tags, "GPSLatitude")
      lonRaw <- find[String](tags, "GPSLongitude")
    } yield CoordinateParseUtils.parseLatLng(format(latRaw), format(lonRaw)).getPayload
  }

  private def parseFocalLength(tags: Exiftool): Option[Int] = {
    find[String](tags, "FocalLengthIn35mmFormat", "FocalLength35efl")
      .map(tag => parseLength(tag))
      .filterNot(_ == 0)
  }

  private def parseShotId(tags: Exiftool): Option[Long] = {
    find[Long](tags, "ShutterCount", "ImageCount")
      .orElse(find[String](tags, "ImageUniqueID").map(str => BigInt(str, 16).toInt))
  }

  def parse(file: Path): Option[Exif] = {
    for {
      tags <- getTags(file)
      serialNo = parseSerialNumber(tags)
      shotId = parseShotId(tags)
      shootingAtRaw <- find[String](tags, "DateTimeOriginal")
      shootingAt <- timeParser(shootingAtRaw)
      model = parseModel(tags)
      latLon = parseLatLon(tags)
    } yield {
      Exif(serialNo, shotId, shootingAt, latLon, model)
    }
  }

  def parseDetail(file: Path): Option[ExifDetail] = {
    for {
      tags <- getTags(file)
      camera = parseModel(tags)
      focal = parseFocalLength(tags)
      lens = find[String](tags, "LensModel", "Lens")
        .filterNot(_ == null)
        .filter(_.nonEmpty)
        .filterNot(_ == "Unknown")
      aperture = find[BigDecimal](tags, "FNumber").filterNot(_ < 0.1)
      exposure <- find[Double](tags, "ExposureTime").map(Fraction(_))
        .orElse(find[String](tags, "ExposureTime").map(tag => parseExposure(tag)))
      iso <- find[Int](tags, "ISO")
    } yield ExifDetail(camera, lens, focal, aperture, exposure, iso)
  }

  def parseDebug(file: Path): Unit = {
    val tags = getTags(file)
    require(tags.isDefined, "tags not found")
//    tags.get.iterable.foreach { case (key, value) => println(s"${key}=${value}") }
    val debug = debugElem(tags.get)
    println("--- Basic ---")
    debug("SerialNumber")
    debug("InternalSerialNumber")
    debug("ShutterCount")
    debug("ImageCount")
    debug("ImageUniqueID")
    debug("DateTimeOriginal")
    debug("GPSLatitude")
    debug("GPSLongitude")
    println("--- Detail ---")
    debug("Model")
    debug("Lens")
    debug("LensModel")
    debug("FocalLengthIn35mmFormat")
    debug("FNumber")
    debug("ExposureTime")
    debug("ISO")
  }

  private def debugElem(tags: Exiftool)(name: String): Unit = {
    tags.print(name)
  }
}

case class Exif(
    serialNo: Option[Int],
    shotId: Option[Long],
    shootingAt: LocalDateTime,
    latLon: Option[LatLng],
    camera: String
) {
  lazy val cameraId: Int = serialNo.getOrElse(camera.hashCode)
  lazy val calcShotId: Long = shotId.fold(shootingAt.toInstant(ZoneOffset.ofHours(+9)).toEpochMilli)(_.toLong)
}

case class ExifDetail(
    camera: String,
    lens: Option[String],
    focal: Option[Int],
    aperture: Option[BigDecimal],
    exposureTime: Fraction,
    iso: Int
)
