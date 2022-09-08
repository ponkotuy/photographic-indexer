package com.ponkotuy.geo

import io.circe.*
import io.circe.Decoder.Result
import io.circe.parser.*
import io.circe.generic.auto.*

import java.net.URI
import java.net.http.HttpResponse.BodyHandlers
import java.net.http.{HttpClient, HttpRequest}

// https://wiki.openstreetmap.org/wiki/JA:Nominatim
class Nominatim {
  val Host = "https://nominatim.openstreetmap.org"

  private[this] val httpClient = HttpClient.newHttpClient()

  def reverse(lat: Double, lon: Double): Option[ReverseResult] = {
    val url = HttpUtil.params(s"${Host}/reverse",
      "format" -> "json",
      "lat" -> lat.toString,
      "lon" -> lon.toString
      // If you are making a large number of requests, you will need to set up an "email"
    )
    val req = HttpRequest.newBuilder(URI.create(url))
        .header("Accept-Language", "ja-JP")
        .GET()
        .build()
    val res = httpClient.send(req, JsonHandler)
    for {
      json <- res.body()
      result <- json.as[ReverseResult].toOption
    } yield result
  }
}

case class ReverseResult(
    placeId: Long,
    license: String,
    osmId: Long,
    lat: BigDecimal,
    lon: BigDecimal,
    displayName: Array[String],
    postcode: String,
    country: String
)

object ReverseResult {
  implicit val decoder: Decoder[ReverseResult] = (c: HCursor) => for {
    placeId <- c.downField("place_id").as[Long]
    license <- c.downField("licence").as[String]
    osmId <- c.downField("osm_id").as[Long]
    lat <- c.downField("lat").as[String].map(BigDecimal.apply)
    lon <- c.downField("lat").as[String].map(BigDecimal.apply)
    displayName <- c.downField("display_name").as[String].map(_.split(",").map(_.trim).dropRight(2))
    address = c.downField("address")
    postcode <- address.downField("postcode").as[String]
    country <- address.downField("country").as[String]
  } yield ReverseResult(placeId, license, osmId, lat, lon, displayName, postcode, country)
}
