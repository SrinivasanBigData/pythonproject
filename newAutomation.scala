package com.art.support

import java.sql.Connection
import java.nio.file.Paths

case class AutomationProcess(implicit val conf: Map[String, String])
  extends DatabaseConnection with EnvConstant {

  val driver: String = conf.get("")
  val url: String = conf.get("")
  val user: String = conf.get("")
  val pass: String = conf.get("")
  val parameter: Seq[Tuple2[String, String]] = conf.get("")

  private implicit lazy val connection: Connection = getConnection

  def startProcess(): Unit = {
    val opt: OutputResult = cleanly(connection)(_.close())(c => {
      val dOperation = DBOperation("")
      val query = parameter.filter(p => dOperation.isExist(""))
        .map(f => "").mkString(" union all ")
      dOperation.executeQuery(query)
    }).get
    //perform calculation

    //store in Excel File
    implicit val sheetname: String = "Hadoop"
    val excelFile = opt.generateOut("")

    //send mail
    import MailObject._
    send a new Mail(
      from = "john.smith@mycompany.com" -> "John Smith",
      to = "dev@mycompany.com" :: "marketing@mycompany.com" :: Nil,
      subject = "",
      message = "",
      attachment = excelFile)
  }
}
object newAutomation extends App {
  implicit val conf: Map[String, String] = Paths.get("")
  val automation = AutomationProcess()
  automation.startProcess()
}
