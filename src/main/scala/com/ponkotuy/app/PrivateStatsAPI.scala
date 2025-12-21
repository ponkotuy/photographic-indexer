package com.ponkotuy.app

import com.ponkotuy.db.ExifStats
import com.ponkotuy.util.Granularity
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

  get("/") {
    (parseGranularity(), parseMetric()) match {
      case (Right(granularity), Right(metric)) =>
        DB.readOnly { implicit session =>
          val data = metric match {
            case "focal_length" => ExifStats.aggregateByFocalLength(granularity)
            case "camera" => ExifStats.aggregateByCamera(granularity)
            case "lens" => ExifStats.aggregateByLens(granularity)
            case "iso" => ExifStats.aggregateByIso(granularity)
          }
          data.asJson.noSpaces
        }
      case (Left(error), _) => BadRequest(error)
      case (_, Left(error)) => BadRequest(error)
    }
  }
}
