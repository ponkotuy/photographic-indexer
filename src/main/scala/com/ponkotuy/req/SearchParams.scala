package com.ponkotuy.req

import com.ponkotuy.db.{ImageFile, Geom, Image}
import org.scalatra.ScalatraServlet
import scalikejdbc.*

case class SearchParams(address: Option[String], path: Option[String]) {
  import Geom.g
  import Image.i

  def query: SQLSyntax = sqls.toAndConditionOpt(
    address.map(againstAddress),
    path.map(againstPath)
  ).getOrElse(sqls"true")

  def orderColumns: Seq[SQLSyntax] = address.map(againstAddress).map(_.desc).toSeq :+ i.id

  private def againstAddress(address: String) =
    sqls"match (${g.address}) against (${address} in natural language mode)"

  private def againstPath(path: String) =
    sqls"match (${ImageFile.column.path}) against (${path} in boolean mode)"
}

trait SearchParamsGenerator { self: ScalatraServlet =>
  def getSearchParams: SearchParams = {
    val address = params.get("address").filterNot(_ == "")
    val pathQuery = params.get("path").filterNot(_ == "")
    SearchParams(address, pathQuery)
  }
}
