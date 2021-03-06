package controller

import model._
import org.joda.time.{ DateTime }

class MemosController extends ApplicationController with Helper {

  def mypage = {
    requireUser { user =>
      val memos = Memos.findByUser(user.id)
      val token = session.get("token").getOrElse("")
      appendCacheControl

      requestScope += ("user" -> Option(user))
      set(Seq(
        "token" -> token,
        "memos" -> memos,
        "urlFor" -> urlFor
      ))
      render("memos/mypage")
    }
  }

  def show = {
    val memoId = params("memoId").toInt
    val user = getUser
    val token = session.get("token").getOrElse("")

    (for {
      m <- Memos.find(memoId)
      if m.isPrivate != 1 || !user.filter { _.id == m.user }.isEmpty
    } yield {
      val memo = m.copy(username = Users.nameOf(m.user), contentHtml = m.content.map { genMarkdown(_) })
      val memos = user.filter { _.id == memo.user }.map { user =>
        Memos.findByUser(memo.user)
      }.getOrElse {
        Memos.findPublicByUser(memo.user)
      }

      val (newerSeq, olderSeq) = memos.span { _.id != memo.id }
      val newer = newerSeq.headOption
      val older = olderSeq.tail.headOption

      requestScope += ("user" -> user)
      requestScope += ("newer" -> newer)
      requestScope += ("older" -> older)
      set(Seq(
        "token" -> token,
        "memo" -> memo,
        "urlFor" -> urlFor
      ))
      render("memos/show")
    }).getOrElse {
      halt(404)
    }
  }

  def create = {
    requireUser { user =>
      antiCsrf {
        val now = new DateTime()
        val memo = Memos.create(
          user = user.id,
          content = params.getAs[String]("content"),
          isPrivate = params.getAs[String]("is_private").map { _.toByte }.getOrElse(0x0),
          createdAt = now,
          updatedAt = now
        )
        redirect(s"/memo/${memo.id}")
      }
    }
  }
}
