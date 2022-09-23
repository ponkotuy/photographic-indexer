package com.ponkotuy.util

import java.util.Locale

object Extensions {
  val Jpeg: Seq[String] = "jpeg" :: "jpg" :: Nil
  val Png: Seq[String] = "png" :: Nil
  val Gif: Seq[String] = "gif" :: Nil
  val Bmp: Seq[String] = "bmp" :: Nil
  val Webp: Seq[String] = "webp" :: Nil
  val Tiff: Seq[String] = "tiff" :: "tif" :: Nil
  val Images: Seq[String] = Jpeg ++ Png ++ Gif ++ Bmp ++ Webp ++ Tiff
  val Raws: Seq[String] =
    "cr2" :: "cr3" :: "crw" :: // Canon
        "raf" :: // FUJIFILM
        "rwl" :: // Leica
        "nef" :: "nrw" :: // Nikon
        "orf" :: // OM
        "rw2" :: // Panasonic
        "pef" :: // PENTAX
        "x3f" :: // SIGMA
        "arw" :: "sr2" :: "srf" :: // SONY
        "dng" :: Nil // Common
  val Retouches: Seq[String] =
    "xmp" :: // Darktable
        "dop" :: // DxO Photolab
        "dr4" :: // Digital Photo Professional
        "nksc" :: // Nikon Capture NX-D
        Nil
  val All: Seq[String] = Images ++ Raws ++ Retouches

  private def toLower(str: String) = str.toLowerCase(Locale.ENGLISH)

  def isRetouchFile(path: String): Boolean = {
    val xs = path.split('.')
    2 < xs.length &&
        ((Raws ++ Images).contains(toLower(xs(1))) || Retouches.contains(toLower(xs.last)))
  }

  def isTarget(path: String): Boolean = ext(path).exists(All.contains)

  def ext(path: String): Option[String] = path.split('.').lastOption.map(toLower)

  def retouchOrigin(path: String): String = {
    val xs = path.split('.')
    require(2 < xs.length, s"${path} is not retouch filename")
    xs.init.mkString(".")
  }

  def contentType(path: String): String = {
    ext(path).getOrElse("") match {
      case x if Jpeg.contains(x) => "image/jpeg"
      case x if Png.contains(x) => "image/png"
      case x if Gif.contains(x) => "image/gif"
      case x if Bmp.contains(x) => "image/bmp"
      case x if Webp.contains(x) => "image/webp"
      case _ => "application/octet-stream"
    }
  }
}
