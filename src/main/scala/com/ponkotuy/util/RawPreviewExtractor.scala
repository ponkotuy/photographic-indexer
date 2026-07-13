package com.ponkotuy.util

import java.nio.file.Path

object RawPreviewExtractor {
  private val PreviewTags = Seq("JpgFromRaw", "PreviewImage", "OtherImage", "ThumbnailImage")

  def largest(path: Path): Option[Array[Byte]] = {
    PreviewTags.flatMap(tag => extractExifBinary(path, tag)).maxByOption(_.length).map { preview =>
      // 埋め込みプレビューにはOrientationが入っていないことがあるので、RAW本体の値を引き継ぐ
      orientation(path).filter(_ != 1).fold(preview)(setOrientation(preview, _))
    }
  }

  private def orientation(path: Path): Option[Int] = {
    val process = new ProcessBuilder("exiftool", "-s3", "-n", "-Orientation", path.toString)
      .redirectError(ProcessBuilder.Redirect.DISCARD)
      .start()
    val stdout = process.getInputStream.readAllBytes()
    val exitCode = process.waitFor()
    if (exitCode == 0) String(stdout).trim.toIntOption else None
  }

  // exiftoolは対象ファイルに`-`を指定するとstdinを読み、編集結果をstdoutへ書く
  private def setOrientation(jpeg: Array[Byte], orientation: Int): Array[Byte] = {
    val process = new ProcessBuilder("exiftool", "-n", s"-Orientation=${ orientation }", "-")
      .redirectError(ProcessBuilder.Redirect.DISCARD)
      .start()
    // stdout読み出しと並行してstdinへ書かないと、パイプバッファが埋まってデッドロックする
    val writer = new Thread(() => {
      try { process.getOutputStream.write(jpeg) }
      finally { process.getOutputStream.close() }
    })
    writer.start()
    val stdout = process.getInputStream.readAllBytes()
    val exitCode = process.waitFor()
    writer.join()
    if (exitCode == 0 && stdout.nonEmpty) stdout else jpeg
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
