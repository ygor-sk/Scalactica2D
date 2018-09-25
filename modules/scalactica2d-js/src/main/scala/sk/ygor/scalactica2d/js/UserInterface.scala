package sk.ygor.scalactica2d.js

import org.querki.jquery._
import org.scalajs.dom
import org.scalajs.dom.raw._
import sk.ygor.scalactica2d.js.animation.{ObjectTreeRenderer, PixelPosition}
import sk.ygor.scalactica2d.js.scenario.ScenarioListService
import sk.ygor.scalactica2d.js.scenario.predefined.EmptyScenario
import sk.ygor.scalactica2d.js.util.Tree
import sk.ygor.scalactica2d.shared.{Elements, MyApi}

class UserInterface(canvas: HTMLCanvasElement,
                    animation: Animation,
                    scenarioListService: ScenarioListService,
                    ajaxClient: AjaxClient)
  extends ObjectTreeRenderer {

  def setup(): Unit = {
    dom.console.log("Scalactica 2D loading")
    setupAjaxCalls()
    setupAnimationControl()
    setupWindowResize()
    setupScenarioList()
    setupKeyboardShortcuts()
    setupWheelZoom()
    setupMouseDrag()

    // explicitly fire event to render first frame
    animation.loadScenario(new EmptyScenario, this)
    $(dom.window).trigger("resize")
  }

  private def setupWheelZoom(): Unit = {
    canvas.onmousewheel = (event: WheelEvent) => {
      animation.zoomBy(event.deltaY)
      false
    }
  }

  private def setupKeyboardShortcuts(): Unit = {
    dom.window.onkeydown = (event: KeyboardEvent) => event.key match {
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

  private def setupAnimationControl(): Unit = {
    $(Elements.toggleAnimation.idSelector).click(() => onToggleAnimation(animation))
  }

  private def setupWindowResize(): Unit = {
    $(dom.window).resize(() => {
      canvas.width = $(canvas).width().toInt
      canvas.height = $(canvas).height().toInt
      animation.resizeTo(canvas.width, canvas.height)
    })
  }

  private def setupAjaxCalls(): Unit = {
    import autowire._

    import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

    $(Elements.dummy.idSelector).click(() => {
      ajaxClient.apply[MyApi].plus(5, 6).call().onComplete(result => {
        dom.console.log(result.toString)
      })
    })
  }

  private def setupMouseDrag(): Unit = {
    case class DragStatus(position: PixelPosition, isOut: Boolean)

    var dragStatus: DragStatus = null

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

    canvas.onmousedown = (e: MouseEvent) => {
      // if (e.ctrlKey) {
      dragStatus = DragStatus(
        PixelPosition(
          e.clientX - canvas.getBoundingClientRect().left,
          e.clientY - canvas.getBoundingClientRect().top
        ),
        isOut = false
      )
      // }
    }

    dom.window.onmousemove = (e: MouseEvent) => {
      if (dragStatus != null) {
        val x = e.clientX - canvas.getBoundingClientRect().left
        val y = e.clientY - canvas.getBoundingClientRect().top
        val deltaX = x - dragStatus.position.x
        val deltaY = y - dragStatus.position.y
        animation.dragBy(deltaX, deltaY)
        dragStatus = dragStatus.copy(position = PixelPosition(x, y))
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

  }

  private def setupScenarioList(): Unit = {
    val scenarios = $(Elements.scenarios.idSelector)
    val prototype = scenarios.find(".prototype")
    scenarioListService.availableScenarios.foreach(scenario => {
      prototype.clone()
        .removeClass("prototype")
        .appendTo(scenarios)
        .show()
        .find("a")
        .html(scenario.name)
        .click(() => animation.loadScenario(scenario, this))
    })
  }


  override def renderObjectTree(tree: Tree): Unit = {
    val spaceObjects = $(Elements.spaceObjects.idSelector)
    val prototype = spaceObjects.find(".prototype")

    spaceObjects.find("li:not(.prototype)").remove()
    tree.allWithLevel.foreach {
      case (spaceObject, level) =>
        dom.console.log((spaceObject, level).toString())

        prototype.clone()
          .removeClass("prototype")
          .appendTo(spaceObjects)
          .show()
          .find("a")
          .html(("-" * level) + s" ${spaceObject.name}")
          .click(() => animation.setFocusedSpaceObject(spaceObject))
    }
  }

  private def onToggleAnimation(animation: Animation): Any = {
    $(Elements.toggleAnimation.idSelector).html(
      if (animation.isAnimationRunning) {
        animation.stopSimulation()
        "Start"
      } else {
        animation.startSimulation()
        "Stop"
      }
    )
  }

}