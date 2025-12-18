package com.ponkotuy.db

object H2SqlConverter {
  def convert(sql: String): Option[String] = {
    val converted = sql
      .replaceAll("(?i)point\\s+srid\\s+\\d+", "geometry")
      .replaceAll("(?i),?\\s*spatial\\s+index\\s*\\([^)]+\\)", "")
      .replaceAll("(?i),?\\s*fulltext\\s*\\([^)]+\\)(\\s+with\\s+parser\\s+\\w+)?", "")
      .replaceAll("(?i)\\s+engine\\s*=\\s*\\w+", "")
      .replaceAll("(?i)\\s+default\\s+charset\\s*=\\s*\\w+", "")
      .replaceAll(",\\s*\\)", ")") // 末尾カンマの修正
      .trim

    // fulltextインデックスのみのALTER文は除外
    if (converted.matches("(?i)alter\\s+table\\s+\\w+\\s+add\\s+fulltext.*")) {
      None
    } else if (converted.isEmpty) {
      None
    } else {
      Some(converted)
    }
  }
}
