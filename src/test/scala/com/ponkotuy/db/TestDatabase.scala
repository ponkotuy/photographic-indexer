package com.ponkotuy.db

import scalikejdbc.*

object TestDatabase {
  private var initialized = false

  def initialize(): Unit =
    if !initialized then {
      Class.forName("org.h2.Driver")
      ConnectionPool.singleton(
        url = "jdbc:h2:mem:test;MODE=MySQL;DB_CLOSE_DELAY=-1",
        user = "sa",
        password = ""
      )
      initialized = true
    }

  def cleanup(): Unit =
    if initialized then {
      ConnectionPool.closeAll()
      initialized = false
    }
}
