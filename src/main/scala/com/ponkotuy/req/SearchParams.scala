package com.ponkotuy.req

import com.ponkotuy.db.{ Geom, Image, ImageFile, Tag }
import org.scalatra.ScalatraServlet
import scalikejdbc.*

import scala.util.matching.Regex

case class SearchParams(keyword: String, tags: Seq[Tag]) {
  import Geom.g
  import Image.i
  import com.ponkotuy.db.Tag.t

  val Space: Regex = "\\s".r
  lazy val words: Array[String] = Space.split(keyword)

  def query: SQLSyntax = {
    val wordGroups = words.map { word =>
      if (tags.exists(_.name == word)) Some(sqls.eq(t.name, word))
      else {
        sqls.toOrConditionOpt(
          Some(againstAddress(word)),
          Some(againstPath(word)),
          Some(againstNote(word))
        )
      }
    }
    sqls.toAndConditionOpt(wordGroups*).getOrElse(sqls"true")
  }

  def orderColumns: Seq[SQLSyntax] = againstNote(keyword).desc :: againstAddress(keyword).desc :: i.id :: Nil

  private def againstAddress(address: String) =
    sqls"match (${ g.address }) against (${ address } in natural language mode)"

  private def againstPath(path: String) =
    sqls"match (${ ImageFile.column.path }) against (${ path } in boolean mode)"

  private def againstNote(note: String) =
    sqls"match (${ i.note }) against (${ note } in natural language mode)"
}

trait SearchParamsGenerator { self: ScalatraServlet =>
  def getSearchParams()(implicit session: DBSession): SearchParams = {
    val keyword = params("keyword")
    val tags = Tag.findAll()
    SearchParams(keyword, tags)
  }
}
