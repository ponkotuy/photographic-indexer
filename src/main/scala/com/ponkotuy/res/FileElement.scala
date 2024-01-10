package com.ponkotuy.res

case class FileElement(fileType: FileType, name: String, imageId: Option[Long] = None)

sealed abstract class FileType

object FileType {
  case object File extends FileType
  case object Directory extends FileType
}