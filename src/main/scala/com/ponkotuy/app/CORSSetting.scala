package com.ponkotuy.app

import org.scalatra.{ Ok, ScalatraServlet }

import scala.util.matching.Regex

trait CORSSetting { self: ScalatraServlet =>
  val Localhost: Regex = "\\Ahttp://localhost(:\\d+)?\\z".r
  val Methods: List[String] = "GET" :: "POST" :: "OPTIONS" :: "DELETE" :: "PUT" :: Nil

  before() {
    val origin = Option(request.getHeader("Origin"))
    if (origin.exists(Localhost.matches)) {
      response.setHeader("Access-Control-Allow-Origin", origin.get)
      response.setHeader("Access-Control-Allow-Methods", Methods.mkString(","))
    }
  }

  options("/*") {
    response.setHeader(
      "Access-Control-Allow-Headers",
      request.getHeader("Access-Control-Request-Headers")
    )
    Ok("")
  }
}
