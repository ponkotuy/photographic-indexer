package com.ponkotuy.db.uitl

import scalikejdbc.*

trait Point {
  def selectX(column: String): SQLSyntax
  def selectY(column: String): SQLSyntax
}

object MySQLPoint extends Point {
  override def selectX(column: String): SQLSyntax = SQLSyntax.createUnsafely(s"ST_X(${ column })")
  override def selectY(column: String): SQLSyntax = SQLSyntax.createUnsafely(s"ST_Y(${ column })")
}
