
package com.art
import scala.util.{ Try, Success, Failure }
import java.sql.{ ResultSet, Connection, Statement, DriverManager }
import java.util.Properties
import java.nio.file.{ Files, Paths, Path }
import java.io.File
import scala.collection.immutable.IndexedSeq

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

  implicit def stringClean(value: Option[String]): String = value.map(_.trim).getOrElse("")

  implicit def stringToSeqTuple(parameter: Option[String]): Seq[Tuple2[String, String]] =
    parameter.split(",")
      .map(s => s.split(":")).
      map { case Array(f1, f2) => (f1, f2) }

  private[art] trait EnvConstant {
    val url_cons = "db.url"
  }

  private[art] trait DatabaseConnection {
    val driver: String
    val url: String
    val user: String
    val pass: String

    @throws[Exception]
    def getConnection: Connection = {
      Class.forName(driver)
      DriverManager.getConnection(url, user, pass)
    }

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

    def isExist(Sql: String): Boolean = {
      false
    }
  }

  private[art] implicit class OutputResultToExcel(rs: OutputResult)(implicit val sheetName: String) {
    import org.apache.poi.hssf.usermodel.HSSFWorkbook
    import org.apache.poi.hssf.usermodel.HSSFFont
    import org.apache.poi.ss.util.CellUtil
    import org.apache.poi.ss.usermodel.IndexedColors

    private lazy val workbook = new HSSFWorkbook()
    private lazy val sheet = workbook.createSheet(sheetName)
    private lazy val boldFont = {
      val font = workbook.createFont()
      font.setFontName(HSSFFont.FONT_ARIAL)
      font.setFontHeightInPoints(10)
      font.setColor(IndexedColors.BLACK.getIndex())
      font.setBold(true)
      font
    }

    private lazy val defaultFont = {
      val font = workbook.createFont()
      font.setFontName(HSSFFont.FONT_ARIAL)
      font.setFontHeightInPoints(10)
      font.setColor(IndexedColors.BLACK.getIndex())
      font.setBold(false)
      font
    }
    private lazy val style = {
      val style = workbook.createCellStyle()
      style
    }
    def generateOut(path: String): File = {
      val row = sheet.createRow(0)
      style.setFont(boldFont)
      row.setRowStyle(style)
      for ((x, i) <- rs.columns.view.zipWithIndex) {
        CellUtil.createCell(row, i, x)
      }
      for ((a, i) <- rs.results.toSeq.view.zipWithIndex) {
        var newRow = sheet.createRow(i + 1)
        style.setFont(defaultFont)
        newRow.setRowStyle(style)
        for ((x, i) <- a.view.zipWithIndex) {
          CellUtil.createCell(row, i, x)
        }
      }
      val outFile = new File(path)
      cleanly(workbook)(_.close())(w => {
        w.write(outFile)
      }).get
      outFile
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
