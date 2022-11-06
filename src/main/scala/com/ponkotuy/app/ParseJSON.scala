package com.ponkotuy.app

import io.circe.*
import io.circe.generic.auto.*
import io.circe.parser.*
import io.circe.syntax.*
import org.scalatra.{ActionResult, BadRequest, ScalatraServlet}

trait ParseJSON { self: ScalatraServlet =>
  def parseJson[T]()(implicit d: Decoder[T]): Either[ActionResult, T] = for {
    json <- parse(request.body).left.map(_ => BadRequest("Fail JSON Parse"))
    data <- json.as[T].left.map(err => BadRequest(err.message))
  } yield data
}
