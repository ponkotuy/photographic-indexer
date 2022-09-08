package com.ponkotuy.geo

import io.circe.*
import io.circe.parser.*

import java.net.http.HttpResponse
import java.net.http.HttpResponse.{BodyHandler, BodySubscriber, BodySubscribers, ResponseInfo}
import java.nio.charset.Charset
import java.util.Optional
import scala.annotation.unused
import scala.jdk.OptionConverters.*
import scala.jdk.FunctionConverters.*

object JsonHandler extends BodyHandler[Option[Json]] {
  override def apply(res: ResponseInfo): BodySubscriber[Option[Json]] = {
    res.headers().firstValue("Content-Type")
    res.statusCode() / 100 match {
      case 2 =>
        val charset = for {
          ct <- ContentType.parse(res)
          if ct.body == "application/json"
          charset <- ct.charset
        } yield Charset.forName(charset)
        BodySubscribers.mapping[String, Option[Json]](
          BodySubscribers.ofString(charset.getOrElse(Charset.defaultCharset())),
          raw => {println(raw); parse(raw).toOption}
        )
      case _ =>
        none
    }
  }

  private def none: BodySubscriber[Option[Json]] = BodySubscribers.replacing(None)
}

case class ContentType(body: String, charset: Option[String])

object ContentType {
  def parse(res: ResponseInfo): Option[ContentType] = {
    for {
      raw <- res.headers().firstValue("Content-Type").toScala
      arr = raw.split(";")
      body <- arr.headOption
    } yield {
      val attrs = arr.tail.map(_.split("="))
      val charset = attrs.filter(_.headOption.contains("charset")).flatMap(_.lift(1)).headOption
      ContentType(body, charset)
    }
  }
}
