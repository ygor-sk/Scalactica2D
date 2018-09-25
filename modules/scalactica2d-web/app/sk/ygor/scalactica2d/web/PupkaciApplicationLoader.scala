package sk.ygor.scalactica2d.web

import _root_.controllers._
import com.softwaremill.macwire.wire
import play.api.ApplicationLoader.Context
import play.api.mvc.EssentialFilter
import play.api.routing.Router
import play.api.{Application, ApplicationLoader, BuiltInComponentsFromContext}
import router.Routes

class Scalactica2dApplicationLoader extends ApplicationLoader {
  override def load(context: Context): Application = {
    new Scalactica2DComponents(context).application
  }
}

class Scalactica2DComponents(context: Context) extends BuiltInComponentsFromContext(context)
  //  with HttpFiltersComponents
  with AssetsComponents
  with Scalactica2dWebComponents {

  // https://github.com/adamw/macwire/issues/82
  // https://www.lucidchart.com/techblog/2018/01/19/compile-time-dependency-injection-with-play/
  lazy val router: Router = {
    implicit val prefix: String = "/"
    wire[Routes]
  }

  def httpFilters: Seq[EssentialFilter] = Seq.empty
}





