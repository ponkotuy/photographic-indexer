package com.ponkotuy.clip

import com.ponkotuy.config.ClipConfig
import com.ponkotuy.http.{HttpClientHolder, HttpUtil, JsonHandler}
import io.circe.Json
import io.circe.generic.auto._

import java.net.URI
import java.net.http.{HttpRequest, HttpResponse}
import java.nio.file.{Files, Path}
import java.util.UUID

class ClipAccessor(conf: ClipConfig) {
  private[this] val httpClient = new HttpClientHolder

  def text(str: String): Option[Array[Float]] = {
    val url = HttpUtil.params(
      conf.uri.resolve("/text").toString,
      "q" -> str
    )
    val req = HttpRequest.newBuilder(URI.create(url)).GET().build()
    val res = httpClient.send(req, JsonHandler)
    parseResponse(res)
  }

  def image(path: Path): Option[Array[Float]] = {
    val uri = conf.uri.resolve("/image")
    val req = buildMultipartBody(uri, path)
    val res = httpClient.send(req, JsonHandler)
    parseResponse(res)
  }

  private def buildMultipartBody(
      uri: URI,
      path: Path,
      boundary: String = UUID.randomUUID().toString
  ): HttpRequest = {
    val fileName = path.getFileName.toString
    val fileBytes = Files.readAllBytes(path)
    val header =
      s"--$boundary\r\nContent-Disposition: form-data; name=\"file\"; filename=\"$fileName\"\r\nContent-Type: application/octet-stream\r\n\r\n"
    val footer = s"\r\n--$boundary--\r\n"
    val body = header.getBytes ++ fileBytes ++ footer.getBytes
    HttpRequest
      .newBuilder(uri)
      .setHeader("Content-Type", s"multipart/form-data; boundary=$boundary")
      .method("GET", HttpRequest.BodyPublishers.ofByteArray(body))
      .build()
  }

  private def parseResponse(
      res: HttpResponse[Option[Json]]
  ): Option[Array[Float]] = {
    for {
      json <- res.body()
      result <- json.as[CLIPResult].toOption
    } yield result.tensor
  }
}

case class CLIPResult(tensor: Array[Float])
