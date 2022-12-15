package com.ponkotuy.req

import com.ponkotuy.db.{ImageFile, Geom}
import org.scalatra.ScalatraServlet
import scalikejdbc.*

case class SearchParams(address: Option[String], path: Option[String]) {
  import Geom.g
  
  def query: SQLSyntax = sqls.toAndConditionOpt(
    address.map(x => sqls"match (${g.address}) against (${x} in natural language mode)"),
    path.map(x => sqls"match(${ImageFile.column.path}) against (${x} in natural language mode)")
  ).getOrElse(sqls"true") 
}

trait SearchParamsGenerator { self: ScalatraServlet =>
  def getSearchParams: SearchParams = {
    val address = params.get("address").filterNot(_ == "")
    val pathQuery = params.get("path").filterNot(_ == "")
    SearchParams(address, pathQuery)
  }
}