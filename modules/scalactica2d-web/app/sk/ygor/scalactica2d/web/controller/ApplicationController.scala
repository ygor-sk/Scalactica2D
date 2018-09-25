package sk.ygor.scalactica2d.web.controller

import controllers.AssetsFinder
import play.api.mvc._
import sk.ygor.scalactica2d.shared.MyApi
import sk.ygor.scalactica2d.web.view
import upickle.Js
import upickle.default._

import scala.concurrent.ExecutionContext


class ApplicationController(controllerComponents: ControllerComponents)
                           (implicit assetsFinder: AssetsFinder)
  extends AbstractController(controllerComponents) {

  def index = Action {
    Ok(view.html.index())
  }

  def api(path: String): Action[AnyContent] = Action.async { request =>
    implicit val ec: ExecutionContext = defaultExecutionContext
    val result = AutowireServer.route[MyApi](MyApiImpl)(
      autowire.Core.Request(
        path.split("\\."),
        upickle.json.read(request.body.asText.get).asInstanceOf[Js.Obj].value.toMap
      )
    )
    result.map(value => Ok(value.toString()))
  }

  object AutowireServer extends autowire.Server[Js.Value, Reader, Writer] {
    def read[Result: Reader](p: Js.Value): Result = upickle.default.readJs[Result](p)

    def write[Result: Writer](r: Result): Js.Value = upickle.default.writeJs(r)
  }

  object MyApiImpl extends MyApi {
    override def plus(x: Int, y: Int): Int = x + y
  }


}
