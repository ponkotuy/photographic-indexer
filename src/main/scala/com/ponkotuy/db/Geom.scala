package com.ponkotuy.db

import scalikejdbc._

case class Geom(id: Long, address: String, lat: Double, lon: Double)

object Geom extends SQLSyntaxSupport[Geom] {
  val g = Geom.syntax("g")

  val select: Seq[SQLSyntax] = g.result.id :: g.result.address :: sqls"ST_X(latlon)" :: sqls"ST_Y(latlon)" :: Nil

  def apply(rn: ResultName[Geom])(rs: WrappedResultSet): Geom =
    Geom(rs.get(rn.id), rs.get(rn.address), rs.get("ST_X(latlon)"), rs.get("ST_Y(latlon)"))

  def create(address: String, lat: Double, lon: Double)(implicit session: DBSession): Long = {
    val latLon = s"POINT($lat $lon)"
    sql"""insert into geom (address, latlon) values ($address, ST_GeomFromText($latLon, 4326))""".updateAndReturnGeneratedKey()
  }
}
