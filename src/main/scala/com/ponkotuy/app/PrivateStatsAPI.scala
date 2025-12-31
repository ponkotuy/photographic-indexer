package com.ponkotuy.app

import com.ponkotuy.db.{ ExifStats, Tag }
import com.ponkotuy.util.{ Granularity, StatsFilter }
import io.circe.generic.auto.*
import io.circe.syntax.*
import org.scalatra.{ BadRequest, ScalatraServlet }
import scalikejdbc.DB

class PrivateStatsAPI extends ScalatraServlet with CORSSetting {

  before() {
    contentType = "application/json; charset=utf-8"
  }

  private val validMetrics = Set("focal_length", "camera", "lens", "iso")

  private def parseGranularity(): Either[String, Granularity] =
    Granularity.parse(
      params.get("granularity"),
      params.get("year").flatMap(_.toIntOption),
      params.get("month").flatMap(_.toIntOption)
    )

  private def parseMetric(): Either[String, String] = {
    params.get("metric") match {
      case Some(m) if validMetrics.contains(m) => Right(m)
      case Some(other) => Left(s"Unknown metric: $other. Use 'focal_length', 'camera', 'lens', or 'iso'")
      case None => Left("Parameter 'metric' is required")
    }
  }

  private def parseFilter(): StatsFilter = {
    StatsFilter(
      camera = params.get("camera").filter(_.nonEmpty),
      lens = params.get("lens").filter(_.nonEmpty),
      tagId = params.get("tagId").flatMap(_.toLongOption)
    )
  }

  get("/") {
    (parseGranularity(), parseMetric()) match {
      case (Right(granularity), Right(metric)) =>
        val filter = parseFilter()
        DB.readOnly { implicit session =>
          val data = metric match {
            case "focal_length" => ExifStats.aggregateByFocalLength(granularity, filter)
            case "camera" => ExifStats.aggregateByCamera(granularity, filter)
            case "lens" => ExifStats.aggregateByLens(granularity, filter)
            case "iso" => ExifStats.aggregateByIso(granularity, filter)
          }
          data.asJson.noSpaces
        }
      case (Left(error), _) => BadRequest(error)
      case (_, Left(error)) => BadRequest(error)
    }
  }

  get("/cameras") {
    DB.readOnly { implicit session =>
      ExifStats.listCameras().asJson.noSpaces
    }
  }

  get("/lenses") {
    DB.readOnly { implicit session =>
      ExifStats.listLenses().asJson.noSpaces
    }
  }

  get("/tags") {
    DB.readOnly { implicit session =>
      Tag.findAll().asJson.noSpaces
    }
  }
}
