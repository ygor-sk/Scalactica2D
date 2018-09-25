package sk.ygor.scalactica2d.js

import org.querki.jquery.$
import org.scalajs.dom
import org.scalajs.dom.CanvasRenderingContext2D
import org.scalajs.dom.raw.HTMLCanvasElement

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

@JSExportTopLevel("Scalactica2dJavascriptApplication")
object Scalactica2dJavascriptApplication {

  @JSExport
  def run(): Unit = {
    val components = new Scalactica2dJavascriptComponents {
      override def canvas: HTMLCanvasElement =
        dom.document.getElementById("scalactica2dCanvas").asInstanceOf[HTMLCanvasElement]

      override def ctx: CanvasRenderingContext2D =
        canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]
    }
    $(() => components.userInterface.setup())
  }

}
