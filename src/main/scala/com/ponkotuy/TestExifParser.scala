package com.ponkotuy

import com.ponkotuy.batch.ExifParser
import com.ponkotuy.config.MyConfig

object TestExifParser {
  def main(args: Array[String]): Unit = {
    val conf = MyConfig.load().getOrElse(throw new RuntimeException("ConfigError"))
    test(conf)
  }

  def test(conf: MyConfig): Unit = {
    testMaker(conf)("Nikon", "20230214/raw/DSC_4525.NEF")
    testMaker(conf)("Panasonic", "20210202-GX7MK3/raw/P1012174.RW2")
    testMaker(conf)("SONY", "20210222-A7C/raw/DSC00282.ARW")
    testMaker(conf)("Canon", "20210228-RP/raw/_MG_0092.CR3")
    testMaker(conf)("FUJIFILM", "20220419/raw/DSCF3559.RAF")
    testMaker(conf)("OM", "old/20201213/raw/_C050069.ORF")
  }

  def testMaker(conf: MyConfig)(maker: String, fileName: String): Unit = {
    println(s"""
               |----------------
               |    ${maker}
               |----------------
               |""".stripMargin)
    val file = conf.app.photosDir.resolve(fileName)
    println(ExifParser.parseDebug(file))
    println(ExifParser.parse(file))
    println(ExifParser.parseDetail(file))
  }
}
