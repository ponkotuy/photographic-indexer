package com.ponkotuy.app

import org.scalatra.ScalatraServlet

import scala.jdk.CollectionConverters.*
import scala.util.matching.Regex

trait CORSSetting { self: ScalatraServlet =>
  val Localhost: Regex = "\\Ahttp://localhost(:\\d+)?\\z".r
  val Methods: List[String] = "GET" :: "POST" :: "OPTIONS" :: "DELETE" :: Nil

  before() {
    val origin = Option(request.getHeader("Origin"))
    if(origin.exists(Localhost.matches)) {
      response.setHeader("Access-Control-Allow-Origin", origin.get)
      response.setHeader("Access-Control-Allow-Methods", Methods.mkString(","))
    }
  }

  options("/*"){
    println(request)
    response.setHeader(
      "Access-Control-Allow-Headers", request.getHeader("Access-Control-Request-Headers")
    )
  }
}
