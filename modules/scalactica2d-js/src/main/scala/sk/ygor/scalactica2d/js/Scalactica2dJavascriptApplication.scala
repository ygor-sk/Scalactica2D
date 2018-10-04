package sk.ygor.scalactica2d.js

import org.querki.jquery.$
import org.scalajs.dom
import org.scalajs.dom.raw.HTMLCanvasElement
import sk.ygor.scalactica2d.shared.Elements

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

@JSExportTopLevel("Scalactica2dJavascriptApplication")
object Scalactica2dJavascriptApplication {

  @JSExport
  def main(): Unit = {
    val components = new Scalactica2dJavascriptComponents {
      override def canvas: HTMLCanvasElement =
        dom.document.getElementById(Elements.scalactica2dCanvas.name).asInstanceOf[HTMLCanvasElement]
    }
    $(() => components.userInterface.setup())
  }

}
