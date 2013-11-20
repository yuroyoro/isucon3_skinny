package controller
import model._

class RootController extends ApplicationController with Helper {

  def index = {
    val user = getUser
    val token = session.get("token").getOrElse("")
    val total = Memos.total
    val memos = Memos.public(0).map {
      m => m.copy(username = Users.nameOf(m.user))
    }

    requestScope += ("user" -> user)
    set(Seq(
      "token" -> token,
      "page" -> 0,
      "memos" -> memos,
      "total" -> total,
      "urlFor" -> urlFor
    ))
    render("/root/index")
  }

  def recent = {
    val page = params("page").toInt
    val total = Memos.total
    val user = getUser
    val token = session.get("token").getOrElse("")

    Memos.public(page) match {
      case Nil => halt(404)
      case xs =>
        val memos = xs.map {
          m => m.copy(username = Users.nameOf(m.user))
        }
        requestScope += ("user" -> user)
        set(Seq(
          "token" -> token,
          "page" -> 0,
          "memos" -> memos,
          "total" -> total,
          "urlFor" -> urlFor
        ))
        render("/root/index")
    }
  }
}

