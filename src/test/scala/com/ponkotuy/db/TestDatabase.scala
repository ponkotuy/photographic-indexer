package com.ponkotuy.db

import scalikejdbc.*

import java.io.File
import java.nio.file.{Files, Path, Paths}
import java.security.MessageDigest
import scala.io.Source
import scala.util.Using

object TestDatabase {
  private var initialized = false
  private val dbPath = "test_db"
  private val migrationDir = "docker/flyway/sql"
  private val hashFile = new File(s"$dbPath.hash")

  def readOnly(f: DBSession => Unit): Unit = {
    if (!initialized) initialize()
    DB.readOnly(f)
  }

  def initialize(): Unit = {
    if (!initialized) {
      val currentHash = computeMigrationHash()
      val savedHash = readSavedHash()

      if (!savedHash.contains(currentHash)) {
        deleteDbFiles()
      }

      Class.forName("org.h2.Driver")
      ConnectionPool.singleton(
        url = s"jdbc:h2:file:./$dbPath;MODE=MySQL;DB_CLOSE_DELAY=-1",
        user = "sa",
        password = ""
      )

      val dbFile = new File(s"$dbPath.mv.db")
      if (!dbFile.exists()) {
        applyMigrations()
        saveHash(currentHash)
      }

      initialized = true
    }
  }

  def cleanup(): Unit = {
    if (initialized) {
      ConnectionPool.closeAll()
      initialized = false
    }
  }

  def deleteDbFiles(): Unit = {
    val dbFile = new File(s"$dbPath.mv.db")
    val traceFile = new File(s"$dbPath.trace.db")
    if (dbFile.exists()) dbFile.delete()
    if (traceFile.exists()) traceFile.delete()
    if (hashFile.exists()) hashFile.delete()
  }

  private def computeMigrationHash(): String = {
    val migrationPath = Paths.get(migrationDir)
    if (Files.exists(migrationPath)) {
      val content = Files.list(migrationPath)
        .toArray
        .map(_.asInstanceOf[Path])
        .filter(_.toString.endsWith(".sql"))
        .sorted
        .flatMap { file =>
          Using(Source.fromFile(file.toFile, "UTF-8"))(_.mkString).toOption
        }
        .mkString

      val md = MessageDigest.getInstance("SHA-256")
      md.digest(content.getBytes("UTF-8")).map("%02x".format(_)).mkString
    } else {
      ""
    }
  }

  private def readSavedHash(): Option[String] = {
    if (hashFile.exists()) {
      Using(Source.fromFile(hashFile, "UTF-8"))(_.mkString.trim).toOption
    } else {
      None
    }
  }

  private def saveHash(hash: String): Unit = {
    Files.writeString(hashFile.toPath, hash)
  }

  private def applyMigrations(): Unit = {
    val migrationPath = Paths.get(migrationDir)
    if (Files.exists(migrationPath)) {
      val versionPattern = "V(\\d+)__.*\\.sql".r
      val sqlFiles = Files.list(migrationPath)
        .toArray
        .map(_.asInstanceOf[Path])
        .filter(_.toString.endsWith(".sql"))
        .sortBy { path =>
          path.getFileName.toString match {
            case versionPattern(version) => version.toInt
            case _ => Int.MaxValue
          }
        }

      DB.autoCommit { implicit session =>
        sqlFiles.foreach { file =>
          Using(Source.fromFile(file.toFile, "UTF-8")) { source =>
            val sql = source.mkString
            sql.split(";").map(_.trim).filter(_.nonEmpty).foreach { statement =>
              H2SqlConverter.convert(statement).foreach { converted =>
                SQL(converted).execute.apply()
              }
            }
          }
        }
      }
    }
  }
}
