package com.ponkotuy.res

case class StatsAggregate(
    period: String,
    category: String,
    min: Option[Int],
    max: Option[Int],
    count: Int
)
