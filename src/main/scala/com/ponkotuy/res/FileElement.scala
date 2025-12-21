package com.ponkotuy.res

import io.circe.Encoder

case class FileElement(fileType: FileType, name: String, imageId: Option[Long] = None)

sealed abstract class FileType

object FileType {
  case object Directory extends FileType
  case object File extends FileType

  val values: Seq[FileType] = Directory :: File :: Nil
  val find: String => FileType = values.view.map(v => v.toString -> v).toMap
  implicit val ordering: Ordering[FileType] = new Ordering[FileType] {
    override def compare(x: FileType, y: FileType): Int = values.indexOf(x).compare(values.indexOf(y))
  }

  implicit val encoder: Encoder[FileType] = Encoder.encodeString.contramap(_.toString)
}
