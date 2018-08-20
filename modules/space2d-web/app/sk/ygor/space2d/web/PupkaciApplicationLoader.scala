package sk.ygor.space2d.web

import _root_.controllers._
import com.softwaremill.macwire.wire
import play.api.ApplicationLoader.Context
import play.api.mvc.EssentialFilter
import play.api.routing.Router
import play.api.{Application, ApplicationLoader, BuiltInComponentsFromContext}
import router.Routes

class Space2dApplicationLoader extends ApplicationLoader {
  override def load(context: Context): Application = {
    new Space2dComponents(context).application
  }
}

class Space2dComponents(context: Context) extends BuiltInComponentsFromContext(context)
  //  with HttpFiltersComponents
  with AssetsComponents
  with Space2dWebComponents {

  // https://github.com/adamw/macwire/issues/82
  // https://www.lucidchart.com/techblog/2018/01/19/compile-time-dependency-injection-with-play/
  lazy val router: Router = {
    implicit val prefix: String = "/"
    wire[Routes]
  }

  def httpFilters: Seq[EssentialFilter] = Seq.empty
}





