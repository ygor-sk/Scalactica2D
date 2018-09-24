package sk.ygor.space2d.js

import org.querki.jquery.$
import org.scalajs.dom
import org.scalajs.dom.CanvasRenderingContext2D
import org.scalajs.dom.raw.HTMLCanvasElement

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

@JSExportTopLevel("Space2dJavascriptApplication")
object Space2dJavascriptApplication {

  @JSExport
  def run(): Unit = {
    val components = new Space2dJavascriptComponents {
      override def canvas: HTMLCanvasElement =
        dom.document.getElementById("space2dCanvas").asInstanceOf[HTMLCanvasElement]

      override def ctx: CanvasRenderingContext2D =
        canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]
    }
    $(() => components.userInterface.setup())
  }

}
