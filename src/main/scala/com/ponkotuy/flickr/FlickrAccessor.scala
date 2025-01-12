package com.ponkotuy.flickr

import com.flickr4java.flickr.Flickr.SAFETYLEVEL_RESTRICTED
import com.flickr4java.flickr.photos.Photo
import com.flickr4java.flickr.{ Flickr, REST }

import scala.jdk.CollectionConverters.*

class FlickrAccessor(apiKey: String, secret: String) {
  private[this] val api = new Flickr(apiKey, secret, new REST())

  /** @param userId:
    *   NSID like "191519170@N08"
    * @param page:
    *   1-based
    */
  def getPeoplePhotos(userId: NSID, page: Int, perPage: Int = 100): Seq[FlickrPhoto] = {
    api.getPeopleInterface.getPhotos(
      userId.underlying,
      SAFETYLEVEL_RESTRICTED,
      null,
      null,
      null,
      null,
      null,
      null,
      Set("description", "geo", "tags", "url_o", "machine_tags").asJava,
      perPage,
      page
    ).asScala.toVector.map(FlickrPhoto.apply)
  }
}

class NSID(val underlying: String) extends AnyVal

case class FlickrPhoto(
    id: String,
    title: String,
    description: Option[String],
    tags: Seq[String],
    originalUrl: String,
    flickrUrl: String,
    isPublic: Boolean
)

object FlickrPhoto {
  def apply(photo: Photo): FlickrPhoto = {
    new FlickrPhoto(
      photo.getId,
      photo.getTitle,
      Option(photo.getDescription),
      photo.getTags.asScala.toSeq.map(_.getValue),
      photo.getOriginalUrl,
      photo.getUrl,
      photo.isPublicFlag
    )
  }
}
