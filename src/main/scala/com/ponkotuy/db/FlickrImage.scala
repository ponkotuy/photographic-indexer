package com.ponkotuy.db

import scalikejdbc.*

case class FlickrImage(imageId: Long, url: String)

object FlickrImage extends SQLSyntaxSupport[FlickrImage] {
  val fi = FlickrImage.syntax("fi")

  def apply(rn: ResultName[FlickrImage])(rs: WrappedResultSet): FlickrImage =
    autoConstruct(rs, rn)

  def find(imageId: Long)(implicit session: DBSession): Unit = withSQL {
    select.from(FlickrImage as fi).where.eq(fi.imageId, imageId)
  }.map(FlickrImage(fi.resultName)).single.apply()

  def create(imageId: Long, url: String)(implicit session: DBSession): Unit = withSQL {
    insert.into(FlickrImage).namedValues(
      column.imageId -> imageId,
      column.url -> url
    )
  }.update.apply()
}
