package com.ponkotuy.db

import scalikejdbc._

case class Geom(id: Long, address: String, lat: Double, lon: Double)

object Geom {
  def create(address: String, lat: Double, lon: Double)(implicit session: DBSession): Long = {
    val latLon = s"POINT($lat $lon)"
    sql"""insert into geom (address, latlon) values ($address, ST_GeomFromText($latLon, 4326))""".updateAndReturnGeneratedKey()
  }
}
