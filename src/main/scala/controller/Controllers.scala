package controller

import skinny._

object Controllers {

  val root = new RootController with Routes {
    get("/?")(index).as('index)
    get("/recent/:page")(recent).as('recent)
  }

  val auth = new AuthController with Routes {
    post("/signout")(signout).as('signout)
    get("/signin")(index).as('index)
    post("/signin")(signin).as('signin)
  }

  val memos = new MemosController with Routes {
    get("/mypage")(mypage).as('mypage)
    get("/memo/:memoId")(show).as('show)
    post("/memo")(create).as('create)
  }
}

