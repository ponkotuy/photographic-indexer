package com.ponkotuy.clip

import com.ponkotuy.config.ClipConfig
import com.ponkotuy.http.{ HttpClientHolder, HttpUtil, JsonHandler }
import io.circe.Json
import io.circe.generic.auto._
import io.github.yskszk63.jnhttpmultipartformdatabodypublisher.MultipartFormDataBodyPublisher

import java.net.URI
import java.net.http.{ HttpRequest, HttpResponse }
import java.nio.file.{ Files, Path, Paths }
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
    val body = new MultipartFormDataBodyPublisher()
      .addFile("file", path)
    val req = HttpRequest.newBuilder(uri)
      .setHeader("Content-Type", body.contentType())
      .method("GET", body)
      .build()
    val res = httpClient.send(req, JsonHandler)
    parseResponse(res)
  }

  private def parseResponse(res: HttpResponse[Option[Json]]): Option[Array[Float]] = {
    for {
      json <- res.body()
      result <- json.as[CLIPResult].toOption
    } yield result.tensor
  }
}

case class CLIPResult(tensor: Array[Float])
