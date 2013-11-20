import skinny._
import skinny.controller._
import _root_.controller._

class ScalatraBootstrap extends SkinnyLifeCycle with grizzled.slf4j.Logging{

  override def initSkinnyApp(ctx: ServletContext) {
    if(SkinnyEnv.isProduction() ){
      info("Skinny Env: Production")
    } else {
      info("Skinny Env: Develop")
    }

    Controllers.root.mount(ctx)
    Controllers.auth.mount(ctx)
    Controllers.memos.mount(ctx)
  }
}

