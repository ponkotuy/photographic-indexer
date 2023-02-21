package com.ponkotuy.batch

import java.nio.file.Path
import sys.process._
import io.circe._
import io.circe.parser._

object Exiftool {
  def run(files: Path*): Seq[Exiftool] = {
    val str = s"exiftool ${files.mkString(" ")} -json".!!
    decode[Seq[JsonObject]](str).fold(err => { println(err.getMessage); Nil }, identity).map(Exiftool(_))
  }
}

case class Exiftool(json: JsonObject) {
  def get[T](name: String)(implicit decoder: Decoder[T]): Option[T] = json(name).flatMap(_.as[T].toOption)
  def apply[T](name: String)(implicit decoder: Decoder[T]): T = get[T](name).get
  def contains(name: String): Boolean = json.contains(name)
  def print(name: String): Unit = println(s"${name}: ${json(name)}")
  def iterable = json.toIterable
}
