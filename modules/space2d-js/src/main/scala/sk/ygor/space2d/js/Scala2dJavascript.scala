package sk.ygor.space2d.js

import org.querki.jquery._
import org.scalajs.dom
import org.scalajs.dom.raw.HTMLCanvasElement

object Scala2dJavascript {

  def main(args: Array[String]): Unit = {
    $(() => {
      dom.console.log("Space2d loading")
      val canvas = dom.document.getElementById("space2dCanvas").asInstanceOf[HTMLCanvasElement]
      val animation = new Scala2dAnimation(canvas)
      $(Elements.toggleAnimation.idSelector).click(() => onToggleAnimation(animation))
      $(dom.window).resize(() => onWindowResize(canvas, animation))
      onWindowResize(canvas, animation)
      $(canvas).on("wheel", (event: JQueryEventObject) => {
        dom.console.log(event)
        false
      })
    })
  }

  private def onToggleAnimation(animation: Scala2dAnimation): Any = {
    $(Elements.toggleAnimation.idSelector).html(
      if (animation.isAnimationRunning) {
        animation.stopAnimation()
        dom.console.log("stopping")
        "Start"
      } else {
        animation.startAnimation()
        dom.console.log("starting")
        "Stop"
      }
    )
  }

  private def onWindowResize(canvas: HTMLCanvasElement, animation: Scala2dAnimation): Any = {
    animation.resizeTo($(canvas).width(), $(canvas).height())
  }

  case class Element(name: String) extends AnyVal {
    def idSelector = s"#$name"

    override def toString: String = name
  }

  object Elements {
    val toggleAnimation = Element("toggleAnimation")
  }

}