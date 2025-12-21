package com.ponkotuy.db

import scalikejdbc.{ DB, DBSession }

object TestDataInitializer {
  val data: TestData = TestData(
    TestData.Image(1L, 1L)
  )

  def generate(): Unit = DB.localTx { implicit session: DBSession =>
    TestDataGenerator.image(id = data.image.id, fileId = data.image.fileId)
  }
}

case class TestData(image: TestData.Image)

object TestData {
  case class Image(id: Long, fileId: Long)
}
