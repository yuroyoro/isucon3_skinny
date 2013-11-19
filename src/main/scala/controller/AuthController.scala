package controller
import model._

class AuthController extends ApplicationController with Helper {

  def signout = {
    requireUser { user =>
      antiCsrf {
        session.invalidate
        redirect("/")
      }
    }
  }

  def index = {
    val user = getUser
    requestScope += ("user" -> user)
    set(Seq(
      "urlFor" -> urlFor
    ))
    render("/auth/signin")
  }

  def signin = {
    (for {
      username <- params.getAs[String]("username")
      password <- params.getAs[String]("password")
      user <- Users.findByName(username)
      if user.password == sha256(user.salt + password)
    } yield {
      session.invalidate

      user.updateLastAccess
      appendCacheControl
      session("user_id") = user.id.toString
      session("token") = sha256(scala.util.Random.nextInt.toString)

      redirect("/mypage")
    }).getOrElse {
      requestScope += ("user" -> None)
      set(Seq(
        "urlFor" -> urlFor
      ))
      render("/auth/signin")
    }
  }
}
