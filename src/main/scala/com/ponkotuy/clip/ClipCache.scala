package com.ponkotuy.clip

import com.ponkotuy.config.ClipConfig
import com.ponkotuy.db.ImageClipIndex
import scalikejdbc.AutoSession

class ClipCache(conf: ClipConfig) {
  lazy val accessor = new ClipAccessor(conf)
  private[this] var cache: Seq[ImageClipIndex] = Nil

  private def cacheCheck(): Unit = {
    if(cache.length < ImageClipIndex.count()(AutoSession).get) cache = ImageClipIndex.findAll()(AutoSession)
  }

  def search(text: String): Option[Seq[ClipSearchResult]] = {
    cacheCheck()
    accessor.text(text).map { textVector =>
      val length = textVector.length
      cache.map { img =>
        val score = (0 until length).map(i => textVector(i) * img.clipIndex(i)).sum
        ClipSearchResult(img.imageId, score)
      }
    }
  }
}

case class ClipSearchResult(imageId: Long, score: Float)
