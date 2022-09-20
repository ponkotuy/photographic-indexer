package com.ponkotuy.app

import org.scalatra.ScalatraServlet
import scala.jdk.CollectionConverters._

trait CORSSetting { self: ScalatraServlet =>
  val Localhost = "\\Ahttp://localhost(:\\d+)?\\z".r

  before() {
    val origin = Option(request.getHeader("Origin"))
    if(origin.exists(Localhost.matches)) {
      response.setHeader("Access-Control-Allow-Origin", origin.get)
    }
  }
}
