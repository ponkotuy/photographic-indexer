package com.ponkotuy.util

case class StatsFilter(
    camera: Option[String] = None,
    lens: Option[String] = None,
    tagId: Option[Long] = None
) {
  def isEmpty: Boolean = camera.isEmpty && lens.isEmpty && tagId.isEmpty
  def nonEmpty: Boolean = !isEmpty
}
