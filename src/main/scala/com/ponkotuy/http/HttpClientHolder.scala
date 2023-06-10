package com.ponkotuy.http

import java.io.IOException
import java.net.http.HttpResponse.BodyHandler
import java.net.http.{HttpClient, HttpRequest, HttpResponse}

class HttpClientHolder {
  private[this] var httpClient = HttpClient.newHttpClient()

  def send[T](req: HttpRequest, res: BodyHandler[T]): HttpResponse[T] = try {
    httpClient.send(req, res)
  } catch {
    case e: IOException =>
      println(s"error: ${e.getMessage}\nReconnection...")
      reconnect()
      send(req, res)
  }

  def reconnect(): Unit = {
    httpClient = HttpClient.newHttpClient()
  }
}
