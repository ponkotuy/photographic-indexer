package com.ponkotuy.app

import org.scalatra.ScalatraServlet

trait CORSSetting { self: ScalatraServlet =>
  val Localhost = "\\Ahttp://localhost(:\\d+)?\\z".r
  before() {
    val origin = request.getHeader("Origin")
    if(Localhost.matches(origin)) {
      response.setHeader("Access-Control-Allow-Origin", origin)
    }
  }
}
