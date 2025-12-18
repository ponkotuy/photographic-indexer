package com.ponkotuy.db

import munit.FunSuite
import scalikejdbc.*

class TestDatabaseSuite extends FunSuite {
  test("select 1") {
    TestDatabase.readOnly { implicit session =>
      val result = sql"select 1".map(_.int(1)).single.apply()
      assertEquals(result, Some(1))
    }
  }
}
