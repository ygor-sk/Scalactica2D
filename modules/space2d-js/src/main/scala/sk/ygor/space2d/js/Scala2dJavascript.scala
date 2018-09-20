package sk.ygor.space2d.js

import org.querki.jquery._
import org.scalajs.dom
import org.scalajs.dom.raw._

import scala.scalajs.js.annotation.JSExport

@JSExport("Scala2dJavascript")
object Scala2dJavascript {

  case class DragStatus(x: Double, y: Double, isOut: Boolean)

  var dragStatus: DragStatus = _

  @JSExport
  def main(): Unit = {
    $(() => {
      dom.console.log("Space2d loading")

      // prepare canvas and it's animation
      val canvas = dom.document.getElementById("space2dCanvas").asInstanceOf[HTMLCanvasElement]
      val animation = new Scala2dAnimation(canvas)

      // bind events
      $(Elements.toggleAnimation.idSelector).click(() => onToggleAnimation(animation))
      $(dom.window).resize(() => onWindowResize(canvas, animation))

      canvas.oncontextmenu = (e: MouseEvent) => {
        e.preventDefault()
        false
      }

      dom.document.onselectstart = (_: Event) => {
        if (dragStatus != null) {
          // do not select text outside if animation during drag
          false
        }
      }


      canvas.onmousewheel = onWheel(animation)
      dom.window.onkeydown = onKeyDown(animation)

      canvas.onmousedown = (e: MouseEvent) => {
        // if (e.ctrlKey) {
        dragStatus = DragStatus(
          e.clientX - canvas.getBoundingClientRect().left,
          e.clientY - canvas.getBoundingClientRect().top,
          isOut = false
        )
        // }
      }

      dom.window.onmousemove = (e: MouseEvent) => {
        if (dragStatus != null) {
          val x = e.clientX - canvas.getBoundingClientRect().left
          val y = e.clientY - canvas.getBoundingClientRect().top
          val deltaX = x - dragStatus.x
          val deltaY = y - dragStatus.y
          animation.dragBy(deltaX, deltaY)
          dragStatus = dragStatus.copy(x = x, y = y)
        }
      }

      canvas.onmouseout = (_: MouseEvent) => {
        if (dragStatus != null) {
          dragStatus = dragStatus.copy(isOut = true)
        }
      }

      canvas.onmouseover = (_: MouseEvent) => {
        if (dragStatus != null) {
          dragStatus = dragStatus.copy(isOut = false)
        }
      }

      dom.window.onmouseup = (_: MouseEvent) => {
        dragStatus = null
      }


      dom.window.ondragstart = (_: MouseEvent) => {
        false
      }
      //      $(canvas).bind("dragstart", (_: dom.Element, event: JQueryEventObject) => {
      //        dom.console.log(event)
      //        false
      //      })
      // http://jsfiddle.net/r0Lowuc7/

      // explicitly fire event to render first frame
      onWindowResize(canvas, animation)
    })
  }


  private def onWheel(animation: Scala2dAnimation)(event: WheelEvent): Boolean = {
    animation.zoomBy(event.deltaY)
    false
  }

  private def onKeyDown(animation: Scala2dAnimation)(event: KeyboardEvent): Boolean = {
    event.key match {
      case "+" =>
        animation.zoomBy(-100)
        false
      case "-" =>
        animation.zoomBy(+100)
        false
      case "ArrowRight" =>
        animation.dragBy(-100, 0)
        false
      case "ArrowLeft" =>
        animation.dragBy(100, 0)
        false
      case "ArrowUp" =>
        animation.dragBy(0, 100)
        false
      case "ArrowDown" =>
        animation.dragBy(0, -100)
        false
      case "s" | "S" =>
        onToggleAnimation(animation)
        false
      case _ =>
        dom.console.log(event.keyCode)
        dom.console.log(event.key)
        true
    }
  }

  private def onToggleAnimation(animation: Scala2dAnimation): Any = {
    $(Elements.toggleAnimation.idSelector).html(
      if (animation.isAnimationRunning) {
        animation.stopSimulation()
        dom.console.log("stopping")
        "Start"
      } else {
        animation.startSimulation()
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