package com.ponkotuy.db.util

import com.ponkotuy.db.uitl.Point
import scalikejdbc.*

object DummyPoint extends Point {
  override def selectX(column: String): SQLSyntax = sqls"139.767061"
  override def selectY(column: String): SQLSyntax = sqls"35.68114"
}
