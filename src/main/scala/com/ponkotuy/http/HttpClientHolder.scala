package com.ponkotuy.http

import java.io.IOException
import java.net.http.HttpResponse.BodyHandler
import java.net.http.{ HttpClient, HttpRequest, HttpResponse }

import scala.concurrent.duration._
import scala.jdk.DurationConverters._

class HttpClientHolder {
  private var httpClient = HttpClient.newHttpClient()

  def send[T](req: HttpRequest, res: BodyHandler[T]): HttpResponse[T] =
    try {
      httpClient.send(req, res)
    } catch {
      case e: IOException =>
        println(s"error: ${ e.getMessage }\nReconnection...")
        Thread.sleep(1.second.toMillis)
        reconnect()
        send(req, res)
    }

  def reconnect(): Unit = {
    httpClient = HttpClient.newHttpClient()
  }
}
