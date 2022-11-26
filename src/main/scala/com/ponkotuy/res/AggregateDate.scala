package com.ponkotuy.res

import com.ponkotuy.db.Image

import java.time.LocalDate

case class AggregateDate(date: LocalDate, imageCount: Int, favoriteImage: Image)

object AggregateDate {
  def fromImages(date: LocalDate, images: Seq[Image]): AggregateDate = {
    require(images.nonEmpty)
    val favorite = images.maxBy(img => (img.tags.length, img.files.length))
    AggregateDate(date, images.length, favorite)
  }
}
