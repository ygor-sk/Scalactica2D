package sk.ygor.scalactica2d.js

import org.scalajs.dom
import upickle.Js
import upickle.default.{readJs, writeJs, _}

import scala.concurrent.Future
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

class AjaxClient extends autowire.Client[Js.Value, Reader, Writer] {

  override def doCall(req: Request): Future[Js.Value] = {
    dom.ext.Ajax.post(
      url = "/api/" + req.path.mkString("."),
      data = upickle.json.write(Js.Obj(req.args.toSeq: _*))
    ).map(_.responseText)
      .map(upickle.json.read)
  }

  def read[Result: Reader](p: Js.Value): Result = readJs[Result](p)

  def write[Result: Writer](r: Result): Js.Value = writeJs(r)
}
