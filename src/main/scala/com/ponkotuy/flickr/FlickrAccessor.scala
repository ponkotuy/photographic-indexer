package com.ponkotuy.flickr

import io.circe.*
import io.circe.parser.*

import java.net.{ URI, URLEncoder }
import java.net.http.{ HttpClient, HttpRequest, HttpResponse }
import java.nio.charset.StandardCharsets

class FlickrAccessor(apiKey: String, secret: String) {
  private val client = HttpClient.newHttpClient()
  private val baseUrl = "https://api.flickr.com/services/rest/"
  private val userAgent = "photographic-indexer/1.0"

  /** @param userId:
    *   NSID like "191519170@N08"
    * @param page:
    *   1-based
    */
  def getPeoplePhotos(userId: NSID, page: Int, perPage: Int = 100): Seq[FlickrPhoto] = {
    val extras = "description,geo,tags,url_o,machine_tags"
    val params = Map(
      "method" -> "flickr.people.getPhotos",
      "api_key" -> apiKey,
      "user_id" -> userId.underlying,
      "extras" -> extras,
      "per_page" -> perPage.toString,
      "page" -> page.toString,
      "format" -> "json",
      "nojsoncallback" -> "1"
    )

    val queryString = params.map { case (k, v) =>
      s"${URLEncoder.encode(k, StandardCharsets.UTF_8)}=${URLEncoder.encode(v, StandardCharsets.UTF_8)}"
    }.mkString("&")

    val request = HttpRequest.newBuilder()
      .uri(URI.create(s"$baseUrl?$queryString"))
      .header("User-Agent", userAgent)
      .GET()
      .build()

    val response = client.send(request, HttpResponse.BodyHandlers.ofString())

    if (response.statusCode() != 200) {
      throw new RuntimeException(s"Flickr API error: ${response.statusCode()} - ${response.body()}")
    }

    parsePhotosResponse(response.body())
  }

  private def parsePhotosResponse(json: String): Seq[FlickrPhoto] = {
    parse(json).toTry.get.hcursor
      .downField("photos")
      .downField("photo")
      .as[Seq[FlickrPhotoJson]].toTry.get
      .map(_.toFlickrPhoto)
  }
}

private case class FlickrPhotoJson(
    id: String,
    owner: String,
    title: String,
    description: Option[ContentWrapper],
    tags: Option[String],
    url_o: Option[String],
    ispublic: Int
) {
  def toFlickrPhoto: FlickrPhoto = FlickrPhoto(
    id = id,
    title = title,
    description = description.map(_._content),
    tags = tags.map(_.split("\\s+").toSeq).getOrElse(Seq.empty),
    originalUrl = url_o.getOrElse(""),
    flickrUrl = s"https://www.flickr.com/photos/$owner/$id",
    isPublic = ispublic == 1
  )
}

private case class ContentWrapper(_content: String)

private object FlickrPhotoJson {
  given Decoder[ContentWrapper] = Decoder.forProduct1("_content")(ContentWrapper.apply)
  given Decoder[FlickrPhotoJson] = Decoder.forProduct7(
    "id", "owner", "title", "description", "tags", "url_o", "ispublic"
  )(FlickrPhotoJson.apply)
}

class NSID(val underlying: String) extends AnyVal

case class FlickrPhoto(
    id: String,
    title: String,
    description: Option[String],
    tags: Seq[String],
    originalUrl: String,
    flickrUrl: String,
    isPublic: Boolean
)
