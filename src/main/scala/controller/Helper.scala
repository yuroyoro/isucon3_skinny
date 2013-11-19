package controller

import model._

trait Helper {
  self: ApplicationController =>

  import sys.process._
  import java.io._

  def requireUser(action: Users => Any): Any = {
    getUser.map { user =>
      appendCacheControl
      action(user)
    }.getOrElse {
      redirect("/")
    }
  }

  def antiCsrf(action: => Any): Any = {
    (for {
      sid <- params.getAs[String]("sid")
      token <- session.get("token")
      if sid == token
    } yield {
      action
    }).getOrElse {
      halt(400)
    }
  }

  def getUser: Option[Users] = {
    scala.Console.println(s"session user_id ${session.get("user_id")}")

    session.get("user_id").flatMap { user_id => Users.find(user_id.toString.toInt) }
  }

  def appendCacheControl: Unit =
    response.setHeader("Cache-Control", "private")

  val markdownCmd = "/Users/ozaki/dev/isucon/webapp/bin/markdown"
  // val markdownCmd = "/home/isucon/webapp/bin/markdown"
  def genMarkdown(md: String): String = {
    val tmp = File.createTempFile("isucontemp", "")
    val out = new BufferedWriter(new FileWriter(tmp))
    out.write(md)
    out.close

    val html = s"${markdownCmd} ${tmp.getAbsolutePath}"!!

    tmp.delete
    html
  }

  def urlFor: String => String = (path: String) => s"http://${request.getServerName}:8080${path}"

  def sha256(s: String) = {
    import java.security.MessageDigest
    val digestedBytes = MessageDigest.getInstance("SHA-256").digest(s.getBytes)
    digestedBytes.map("%02x".format(_)).mkString
  }
}

