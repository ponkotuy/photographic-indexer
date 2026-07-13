package com.ponkotuy.util

import java.nio.file.Path

object ExifOrientation {
  def read(path: Path): Option[Int] = {
    val process = new ProcessBuilder("exiftool", "-s3", "-n", "-Orientation", path.toString)
      .redirectError(ProcessBuilder.Redirect.DISCARD)
      .start()
    val stdout = process.getInputStream.readAllBytes()
    val exitCode = process.waitFor()
    if (exitCode == 0) String(stdout).trim.toIntOption else None
  }

  // exiftoolは対象ファイルに`-`を指定するとstdinを読み、編集結果をstdoutへ書く
  def write(jpeg: Array[Byte], orientation: Int): Array[Byte] = {
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
}
