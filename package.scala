
package com.art
import scala.util.{ Try, Success, Failure }
import java.sql.ResultSet
import java.io.OutputStream
import java.sql.Connection
import scala.collection.immutable.IndexedSeq
import java.sql.Statement
import java.util.Properties
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.Path

package object support {
  private[art] def cleanly[A, B](resource: A)(cleanup: A => Unit)(doWork: A => B): Try[B] = {
    try {
      Success(doWork(resource))
    } catch {
      case e: Exception => Failure(e)
    } finally {
      try {
        if (resource != null) {
          cleanup(resource)
        }
      } catch {
        case e: Exception => println(e)
      }
    }
  }

  implicit private[art] def getConfiguration(path: Path): Map[String, String] = {
    import scala.collection.JavaConverters._
    val prop = new Properties()
    prop.load(Files.newInputStream(path))
    prop.asScala.toMap
  }

  private[art] trait EnvConstant {
    val url_cons = "db.url"
  }

  private[art] trait DatabaseConnection {
    val driver: String
    val url: String
    val user: String
    val pass: String
    def getConnection: Connection = null
  }

  private[art] case class OutputResult(
    columns: IndexedSeq[String],
    results: Iterator[IndexedSeq[String]])

  private[art] case class DBOperation(val schema: String)(private implicit val connection: Connection) {
    private def executeSql(execSql: String) = {
      val stmt: Statement = connection.createStatement
      val rs: ResultSet = stmt.executeQuery(execSql)
      val columnCnt: Int = rs.getMetaData.getColumnCount
      val columns: IndexedSeq[String] = 1 to columnCnt map rs.getMetaData.getColumnName
      val results: Iterator[IndexedSeq[String]] = Iterator
        .continually(rs).takeWhile(_.next())
        .map { rs =>
          columns map rs.getString
        }
      (columns, results)
    }
    def executeQuery(sql: String): OutputResult = {
      val (d1, d2) = executeSql(sql)
      OutputResult(d1, d2)
    }
  }

  private[art] implicit class ResultSetToExcel(rs: ResultSet)(implicit val sheetname: String) {
    def getSheetName(): String = sheetname
    def generateOut(output: OutputStream): String = {
      """"""
    }
  }

  object MailObject {
    implicit def stringToSeq(single: String): Seq[String] = Seq(single)
    implicit def liftToOption[T](t: T): Option[T] = Some(t)
    case class Mail(
      from:       (String, String),
      to:         Seq[String],
      cc:         Seq[String]            = Seq.empty,
      bcc:        Seq[String]            = Seq.empty,
      subject:    String,
      message:    String,
      attachment: Option[(java.io.File)] = None)
    object send {
      def a(mail: Mail) {

      }
    }
  }
}
