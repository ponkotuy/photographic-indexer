package com.ponkotuy.db

import com.ponkotuy.db.TestDataInitializer.data
import com.ponkotuy.db.util.DummyPoint
import munit.FunSuite
import scalikejdbc.*

class ImageWithAllSuite extends FunSuite {
  Geom.point = DummyPoint
  test("find returns Image with ImageFile") {
    TestDatabase.readOnly { implicit session =>
      val result = ImageWithAll.find(data.image.id, isPublic = false)

      assert(result.isDefined, "Image should be found")
      val image = result.get
      assertEquals(image.id, data.image.id)
      assertEquals(image.files.size, 1)
      assertEquals(image.files.head.id, data.image.fileId)
      assert(image.exif.isDefined)
    }
  }

  test("findRandom returns Image") {
    TestDatabase.readOnly { implicit session =>
      val result = ImageWithAll.findRandom(sqls"true")

      assert(result.isDefined, "Image should be found")
      val image = result.get
      assertEquals(image.id, data.image.id)
      assertEquals(image.files.size, 1)
      assertEquals(image.files.head.id, data.image.fileId)
    }
  }

  test("findRandom returns public Image") {
    TestDatabase.localTx { implicit session =>
      val imageId = TestDataGenerator.image(isPublic = true)
      val result = ImageWithAll.findRandom(ImageWithAll.isPublicSQL)

      assert(result.isDefined, "Public image should be found")
      val image = result.get
      assertEquals(image.id, imageId)
    }
  }
}
