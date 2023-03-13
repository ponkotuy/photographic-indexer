package com.ponkotuy.batch

import java.nio.file.Path
import sys.process._
import io.circe._
import io.circe.parser._
import scala.collection.mutable

object Exiftool {
  def run(files: Path*): Seq[Exiftool] = {
    val logger = new ProcessResultEither
    s"exiftool ${files.mkString(" ")} -json" ! logger
    logger.either.fold(
      err => { println(err); Nil },
      str => decode[Seq[JsonObject]](str).fold(err => { println(err.getMessage); Nil }, identity).map(Exiftool(_))
    )
  }
}

case class Exiftool(json: JsonObject) {
  def get[T](name: String)(implicit decoder: Decoder[T]): Option[T] = json(name).flatMap(_.as[T].toOption)
  def apply[T](name: String)(implicit decoder: Decoder[T]): T = get[T](name).get
  def contains(name: String): Boolean = json.contains(name)
  def print(name: String): Unit = println(s"${name}: ${json(name)}")
  def iterable: Iterable[(String, Json)] = json.toIterable
}

class ProcessResultEither extends ProcessLogger {
  private[this] val stdoutLines = mutable.ArrayBuffer[String]()
  private[this] val stderrLines = mutable.ArrayBuffer[String]()

  override def out(line: => String): Unit = stdoutLines.append(line)
  override def err(line: => String): Unit = stderrLines.append(line)
  override def buffer[T](f: => T): T = f

  def either: Either[String, String] =
    if(stderrLines.nonEmpty) Left(stderrLines.mkString("\n"))
    else Right(stdoutLines.mkString("\n"))
}
