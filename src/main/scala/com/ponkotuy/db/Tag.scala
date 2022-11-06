package com.ponkotuy.db

import scalikejdbc.*

case class Tag(id: Long, name: String)

object Tag extends SQLSyntaxSupport[Tag] {
  val t = Tag.syntax("t")

  def apply(rn: ResultName[Tag])(rs: WrappedResultSet): Tag = autoConstruct(rs, rn)

  def findAll()(implicit session: DBSession): Seq[Tag] = withSQL {
    select.from(Tag as t)
  }.map(Tag(t.resultName)).list.apply()

  def create(name: String)(implicit session: DBSession): Long = withSQL {
    insert.into(Tag).namedValues(column.name -> name)
  }.updateAndReturnGeneratedKey.apply()
}
