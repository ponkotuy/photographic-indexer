package com.ponkotuy.util

import scalikejdbc.*

sealed trait Granularity {
  def dateFormat: String
  def condition(shootingAt: SQLSyntax): Option[SQLSyntax]
}

case object Yearly extends Granularity {
  override def dateFormat: String = "%Y"
  override def condition(shootingAt: SQLSyntax): Option[SQLSyntax] = None
}

case class Monthly(year: Int) extends Granularity {
  override def dateFormat: String = "%Y-%m"
  override def condition(shootingAt: SQLSyntax): Option[SQLSyntax] = Some(
    sqls"year($shootingAt) = $year"
  )
}

case class Daily(year: Int, month: Int) extends Granularity {
  override def dateFormat: String = "%Y-%m-%d"
  override def condition(shootingAt: SQLSyntax): Option[SQLSyntax] = Some(
    sqls"year($shootingAt) = $year and month($shootingAt) = $month"
  )
}

object Granularity {
  def parse(
      granularity: Option[String],
      year: Option[Int],
      month: Option[Int]
  ): Either[String, Granularity] = {
    granularity match {
      case Some("yearly") => Right(Yearly)
      case Some("monthly") =>
        year match {
          case Some(y) => Right(Monthly(y))
          case None => Left("Parameter 'year' is required for monthly granularity")
        }
      case Some("daily") =>
        (year, month) match {
          case (Some(y), Some(m)) => Right(Daily(y, m))
          case _ => Left("Parameters 'year' and 'month' are required for daily granularity")
        }
      case Some(other) => Left(s"Unknown granularity: $other. Use 'yearly', 'monthly', or 'daily'")
      case None => Left("Parameter 'granularity' is required")
    }
  }
}
