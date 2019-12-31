package com.art.support

import java.sql.Connection
import java.nio.file.Paths

case class AutomationProcess(implicit val conf: Map[String, String])
  extends DatabaseConnection with EnvConstant {
  val driver: String = ""
  val url: String = ""
  val user: String = ""
  val pass: String = ""
  val parameter: String = ""

  def startProcess(): Unit = {
    implicit lazy val connection: Connection = getConnection
    val opt = cleanly(connection)(_.close())(c => {
      val dOperation = DBOperation("")

      //dOperation.execute()
    }).get

    //perform calculation
    //store in Excel File
    //send mail
    import MailObject._
    send a new Mail(
      from = ("john.smith@mycompany.com", "John Smith"),
      to = "boss@mycompany.com",
      cc = "hr@mycompany.com",
      subject = "Import stuff",
      message = "Dear Boss...")
  }
}
object newAutomation extends App {
  implicit val conf: Map[String, String] = Paths.get("")
  val automation = AutomationProcess()
  automation.startProcess()
}
