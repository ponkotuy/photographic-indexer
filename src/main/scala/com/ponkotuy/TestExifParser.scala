package com.ponkotuy

import com.ponkotuy.batch.ExifParser
import com.ponkotuy.config.MyConfig

object TestExifParser {
  def main(args: Array[String]): Unit = {
    val conf = MyConfig.load().getOrElse(throw new RuntimeException("ConfigError"))
    test(conf)
  }

  def test(conf: MyConfig): Unit = {
    testPattern(conf)("Nikon", "20230214/raw/DSC_4525.NEF")
    testPattern(conf)("Panasonic", "20210202-GX7MK3/raw/P1012174.RW2")
    testPattern(conf)("SONY", "20210222-A7C/raw/DSC00282.ARW")
    testPattern(conf)("Canon", "20210228-RP/raw/_MG_0092.CR3")
    testPattern(conf)("FUJIFILM", "20220419/raw/DSCF3559.RAF")
    testPattern(conf)("OM", "old/20201213/raw/_C050069.ORF")
    testPattern(conf)("Exposure 1", "20210111-Z6/raw/DSC_1044.NEF")
  }

  def testPattern(conf: MyConfig)(name: String, fileName: String): Unit = {
    println(s"""
               |----------------
               |    ${name}
               |----------------
               |""".stripMargin)
    val file = conf.app.photosDir.resolve(fileName)
    println(ExifParser.parseDebug(file))
    println(ExifParser.parse(file))
    println(ExifParser.parseDetail(file))
  }
}
