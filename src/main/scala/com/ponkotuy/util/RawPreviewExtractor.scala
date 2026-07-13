package com.ponkotuy.util

import java.nio.file.Path

object RawPreviewExtractor {
  private val PreviewTags = Seq("JpgFromRaw", "PreviewImage", "OtherImage", "ThumbnailImage")

  def largest(path: Path): Option[Array[Byte]] = {
    PreviewTags.flatMap(tag => extractExifBinary(path, tag)).maxByOption(_.length).map { preview =>
      // 埋め込みプレビューにはOrientationが入っていないことがあるので、RAW本体の値を引き継ぐ
      ExifOrientation.read(path).filter(_ != 1).fold(preview)(ExifOrientation.write(preview, _))
    }
  }

  private def extractExifBinary(path: Path, tag: String): Option[Array[Byte]] = {
    val process = new ProcessBuilder("exiftool", "-b", s"-$tag", path.toString)
      .redirectError(ProcessBuilder.Redirect.DISCARD)
      .start()
    val stdout = process.getInputStream.readAllBytes()
    val exitCode = process.waitFor()
    if (exitCode == 0 && stdout.nonEmpty) Some(stdout) else None
  }
}
