package com.ponkotuy.db

import com.ponkotuy.db.uitl.{ MySQLPoint, Point }
import scalikejdbc.*

case class Geom(id: Long, address: String, lat: Double, lon: Double)

object Geom extends SQLSyntaxSupport[Geom] {
  // noinspection TypeAnnotation
  val g = Geom.syntax("g")

  var point: Point = MySQLPoint

  lazy val select: Seq[SQLSyntax] = {
    g.result.id :: g.result.address :: point.selectX("latlon") :: point.selectY("latlon") :: Nil
  }

  def apply(rn: ResultName[Geom])(rs: WrappedResultSet): Geom =
    Geom(rs.get(rn.id), rs.get(rn.address), rs.get(point.selectX("latlon")), rs.get(point.selectY("latlon")))

  def create(address: String, lat: Double, lon: Double)(implicit session: DBSession): Long = {
    val latLon = s"POINT($lat $lon)"
    sql"""insert into geom (address, latlon) values ($address, ST_GeomFromText($latLon, 4326))""".updateAndReturnGeneratedKey()
  }
}
