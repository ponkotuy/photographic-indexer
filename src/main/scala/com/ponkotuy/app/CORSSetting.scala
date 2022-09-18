package com.ponkotuy.app

import org.scalatra.ScalatraServlet

trait CORSSetting { self: ScalatraServlet =>
  val Localhost = "\\Ahttp://localhost(:\\d+)?\\z".r

  before() {
    val origin = Option(request.getHeader("Origin"))
    if(origin.exists(Localhost.matches)) {
      response.setHeader("Access-Control-Allow-Origin", origin.get)
    }
  }
}
