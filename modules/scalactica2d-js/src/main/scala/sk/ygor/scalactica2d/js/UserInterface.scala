package sk.ygor.scalactica2d.js

import org.querki.jquery._
import org.scalajs.dom
import org.scalajs.dom.raw.HTMLCanvasElement
import org.scalajs.dom.{Position => _, _}
import sk.ygor.scalactica2d.js.animation.{PixelPosition, PositionConverter}
import sk.ygor.scalactica2d.js.objects.{Marker, MovingObject, SpaceObject, Speck}
import sk.ygor.scalactica2d.js.scenario._
import sk.ygor.scalactica2d.js.scenario.predefined.FirstScenario
import sk.ygor.scalactica2d.js.units._
import sk.ygor.scalactica2d.js.util.SpaceObjectTree
import sk.ygor.scalactica2d.shared.{Elements, MyApi}

class UserInterface(canvas: HTMLCanvasElement,
                    scenarioListService: ScenarioListService,
                    ajaxClient: AjaxClient,
                    scenarioSimulatorFactory: ScenarioSimulatorFactory) extends PositionConverter {

  private val ctx: CanvasRenderingContext2D = canvas.getContext("2d").asInstanceOf[CanvasRenderingContext2D]
  private val scenarioSimulatorListener: ScenarioSimulator.EventListener = createScenarioSimulationListener()

  private val PIXEL_PER_METER: Int = 1000
  private val centerMarker = Marker("[0,0]", Position.zero)

  private var animationRunning: Boolean = false
  private var lastAnimationRequestId: Int = 0

  private var focusedSpaceObject: SpaceObject = _
  private var focusOffset = Position.zero

  private var zoom: Zoom = Zoom(27)

  // FPS
  private var framesDrawn: Int = 0
  private var firstFrameDrawn: Double = 0d

  private var scenarioSimulator: ScenarioSimulator = _

  private var pixelWidth: Double = 0
  private var pixelHeight: Double = 0


  dom.window.setInterval(
    () => {
      dom.console.log(s"FPS: ${getAndResetFps()}")
    },
    2500
  )

  case class Zoom(level: Int) {

    val mainLevel: Int = Math.pow(10, level / 9).toInt
    val subLevel: Int = (level % 9) + 1

    /**
      * 1, 2, 3, .., 9, 10, 20, 30, ..., 90, 100, 200, 300, ..., 900, ...
      */
    val canvasZoom: Int = mainLevel * subLevel
  }

  def toPixelPosition(position: Position): PixelPosition = PixelPosition(
    meterToPixels(position.x - focusPosition.x) + (pixelWidth / 2),
    meterToPixels(position.y - focusPosition.y) + (pixelHeight / 2)
  )

  def meterToPixels(m: Meter): Double = (m.value * PIXEL_PER_METER) / zoom.canvasZoom

  private def pixelToMeters(d: Double): Meter = Meter((d * zoom.canvasZoom) / PIXEL_PER_METER)

  private def focusPosition: Position = focusedSpaceObject.position + focusOffset

  private def draw(time: Double): Unit = {
    if (framesDrawn == 0) {
      firstFrameDrawn = time
    }
    framesDrawn += 1

    ctx.clearRect(0, 0, pixelWidth, pixelHeight)
    drawMesh()
    val allspaceObjects = scenarioSimulator.allSpaceObjects()
    for (i <- allspaceObjects.indices) {
      val spaceObject = allspaceObjects(i)
      spaceObject.draw(ctx, this)
    }
    centerMarker.draw(ctx, this)

    if (animationRunning) {
      scenarioSimulator.calculateNextStep()
      requestAnimationFrame()
    }
  }

  def drawMesh(): Unit = {
    ctx.strokeStyle = "rgb(128,128,128)"

    // TODO: move to rescale
    // TODO: optimize
    val meshMeters = Meter(zoom.mainLevel)
    val meshOffset = focusPosition - Position(focusPosition.x % meshMeters, focusPosition.y % meshMeters)
    val meshPixels = meterToPixels(meshMeters)
    val meshCountX = (pixelWidth / meshPixels / 2).toInt + 1
    val meshCountY = (pixelHeight / meshPixels / 2).toInt + 1
    val meshOffsetPixels = toPixelPosition(meshOffset)

    for (x <- 0 to meshCountX) {
      ctx.beginPath()
      val dx = (meshOffsetPixels.x + x * meshPixels).toInt
      ctx.moveTo(dx, 0)
      ctx.lineTo(dx, pixelHeight)
      if (x > 0) {
        val dx2 = (meshOffsetPixels.x - x * meshPixels).toInt
        ctx.moveTo(dx2, 0)
        ctx.lineTo(dx2, pixelHeight)
      }
      ctx.closePath()
      ctx.stroke()
    }

    for (y <- 0 to meshCountY) {
      ctx.beginPath()
      val dy = (meshOffsetPixels.y + y * meshPixels).toInt
      ctx.moveTo(0, dy)
      ctx.lineTo(pixelWidth, dy)
      if (y > 0) {
        val dy2 = (meshOffsetPixels.y - y * meshPixels).toInt
        ctx.moveTo(0, dy2)
        ctx.lineTo(pixelWidth, dy2)
      }
      ctx.closePath()
      ctx.stroke()
    }
  }

  private def requestAnimationFrame(): Unit = {
    lastAnimationRequestId = dom.window.requestAnimationFrame(draw)
  }

  def getAndResetFps(): Double = {
    val result = framesDrawn / (dom.window.performance.now() - firstFrameDrawn) * 1000
    framesDrawn = 0
    result
  }

  def startSimulation(): Unit = {
    assert(!animationRunning)
    animationRunning = true
    requestAnimationFrame()
  }

  def stopSimulation(): Unit = {
    assert(animationRunning)
    animationRunning = false
    if (lastAnimationRequestId != 0) {
      dom.window.cancelAnimationFrame(lastAnimationRequestId)
    }
  }

  def zoomBy(wheelDelta: Double): Unit = {
    val zoomDelta = (wheelDelta / 100).toInt
    zoom = Zoom(Math.max(zoom.level + zoomDelta, 0))
    dom.console.log(s"zoom set to: $zoom")
    rescale()
  }

  def resizeTo(width: Int, height: Int): Unit = {
    pixelWidth = width
    pixelHeight = height
    rescale()
    dom.console.log(s"window resized: $width / $height")
  }

  def dragBy(deltaX: Double, deltaY: Double): Unit = {
    focusOffset = focusOffset - Position(pixelToMeters(deltaX), pixelToMeters(deltaY))
    rescale()
  }

  def setFocusedSpaceObject(spaceObject: SpaceObject): Unit = {
    focusedSpaceObject = spaceObject
    focusOffset = Position.zero
    setAvailableControls(spaceObject)

    scenarioSimulatorListener.focusedObjectChanged(spaceObject)
    scenarioSimulatorListener.objectPositionChanged(spaceObject)
    Seq(spaceObject).collect {
      case movingObject: MovingObject => scenarioSimulatorListener.objectSpeedChanged(movingObject)
    }

    rescale()
  }

  def loadScenario(scenario: Scenario): Unit = {
    dom.console.log(s"loading scenario: $scenario")
    this.scenarioSimulator = scenarioSimulatorFactory(scenario, scenarioSimulatorListener)

    setFocusedSpaceObject(centerMarker)
    zoom = Zoom(27)
    rescale()

    renderObjectTree(SpaceObjectTree(centerMarker, Seq(scenarioSimulator.currentTree)))
    renderMission(scenario.mission)
  }

  private def createScenarioSimulationListener(): ScenarioSimulator.EventListener = {
    val speedAccelerationElement = $(Elements.speedAcceleration.idSelector)
    val positionXSpan = speedAccelerationElement.find(".positionX")
    val positionYSpan = speedAccelerationElement.find(".positionY")
    val speedXSpan = speedAccelerationElement.find(".speedX")
    val speedYSpan = speedAccelerationElement.find(".speedY")
    val accelerationXSpan = speedAccelerationElement.find(".accelerationX")
    val accelerationYSpan = speedAccelerationElement.find(".accelerationY")

    new ScenarioSimulator.EventListener {

      // TODO: defer to next animation frame

      override def focusedObjectChanged(spaceObject: SpaceObject): Unit = {
        positionXSpan.html("-")
        positionYSpan.html("-")
        speedXSpan.html("-")
        speedYSpan.html("-")
        accelerationXSpan.html("-")
        accelerationYSpan.html("-")
      }

      override def objectPositionChanged(spaceObject: SpaceObject): Unit = {
        if (focusedSpaceObject eq spaceObject) {
          positionXSpan.html(spaceObject.position.x.value.formatted("%.6f"))
          positionYSpan.html(spaceObject.position.y.value.formatted("%.6f"))
        }
      }

      override def objectSpeedChanged(movingObject: MovingObject): Unit = {
        if (focusedSpaceObject eq movingObject) {
          speedXSpan.html(movingObject.speed.x.value.formatted("%.6f"))
          speedYSpan.html(movingObject.speed.y.value.formatted("%.6f"))
        }
      }

      override def objectAccelerationChanged(spaceObject: SpaceObject, acceleration: Acceleration): Unit = {
        if (focusedSpaceObject eq spaceObject) {
          accelerationXSpan.html(acceleration.x.value.formatted("%.6f"))
          accelerationYSpan.html(acceleration.y.value.formatted("%.6f"))
        }
      }
    }
  }

  private def rescale(): Unit = {
    if (!animationRunning) {
      dom.window.requestAnimationFrame(draw)
    }
  }

  def setup(): Unit = {
    dom.console.log("Scalactica 2D loading")
    setupAjaxCalls()
    setupAnimationControl()
    setupWindowResize()
    setupScenarioList()
    setupKeyboardShortcuts()
    setupWheelZoom()
    setupMouseDrag()

    loadScenario(new FirstScenario)
    $(Elements.controlsTab.idSelector).show()
    // explicitly fire event to render first frame
    $(dom.window).trigger("resize")
  }

  private def setupWheelZoom(): Unit = {
    canvas.onmousewheel = (event: WheelEvent) => {
      zoomBy(event.deltaY)
      false
    }
  }

  private def setupKeyboardShortcuts(): Unit = {
    dom.window.onkeydown = (event: KeyboardEvent) => event.key match {
      case "+" if event.ctrlKey =>
        zoomBy(-100)
        false
      case "-" if event.ctrlKey =>
        zoomBy(+100)
        false
      case "ArrowRight" if event.ctrlKey =>
        dragBy(-100, 0)
        false
      case "ArrowLeft" if event.ctrlKey =>
        dragBy(100, 0)
        false
      case "ArrowUp" if event.ctrlKey =>
        dragBy(0, 100)
        false
      case "ArrowDown" if event.ctrlKey =>
        dragBy(0, -100)
        false
      case "s" | "S" if event.ctrlKey =>
        onToggleAnimation()
        false
      case _ =>
        dom.console.log(event.keyCode)
        dom.console.log(event.key)
        true
    }
  }

  private def setupAnimationControl(): Unit = {
    $(Elements.toggleAnimation.idSelector).click(() => onToggleAnimation())
  }

  private def setupWindowResize(): Unit = {
    $(dom.window).resize(() => {
      canvas.width = $(canvas).width().toInt
      canvas.height = $(canvas).height().toInt
      resizeTo(canvas.width, canvas.height)
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
        dragBy(deltaX, deltaY)
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
    val scenarioList = $(Elements.scenarioList.idSelector)
    val prototype = scenarioList.find(".prototype")
    scenarioListService.availableScenarios.foreach(scenario => {
      prototype.clone()
        .removeClass("prototype")
        .appendTo(scenarioList)
        .show()
        .find("a")
        .html(scenario.name)
        .click(() => loadScenario(scenario))

    })
  }


  def renderObjectTree(tree: SpaceObjectTree): Unit = {
    val spaceObjects = $(Elements.spaceObjectTree.idSelector)
    val prototype = spaceObjects.find(".prototype")

    spaceObjects.find("li:not(.prototype)").remove()
    tree.allWithLevel.foreach {
      case (spaceObject, level) =>
        dom.console.log((spaceObject, spaceObject.id, level).toString())

        prototype.clone()
          .removeClass("prototype")
          .appendTo(spaceObjects)
          .show()
          .find("a")
          .html(("-" * level) + s" ${spaceObject.name}")
          .click(() => setFocusedSpaceObject(spaceObject))
    }
  }

  def renderMission(mission: Mission): Unit = {
    val missionElement = $(Elements.mission.idSelector)
    val stepsIterator = mission.steps.iterator
    val current = missionElement.find(".current")
    val advance = missionElement.find(".advance")

    def doRenderNext(): Unit = {
      advance.find("*").hide()
      if (stepsIterator.hasNext) {
        val step = stepsIterator.next()
        current.html(step.description)
        step.advance match {
          case Mission.NextStep =>
            advance.find(".nextStep").show()
              .off("click")
              .click(() => doRenderNext())
          case Mission.Condition(description) =>
            advance.find(".condition").show()
              .html(description)
        }
      } else {
        current.html("Mission acomplished !")
      }
    }

    doRenderNext()
  }


  def setAvailableControls(spaceObject: SpaceObject): Unit = {
    val speedDeltaElement = $(Elements.objectControlSpeedDelta.idSelector).hide()
    Seq(spaceObject).collect {
      case speck: Speck => {
        speedDeltaElement.show()
        speedDeltaElement.find("button").off("click")

        // TODO: remember last value
        var level = 1

        def calculateSpeedDeltaFactor: Double = {
          val mainLevel: Int = Math.pow(10, level / 9).toInt
          val subLevel: Int = (level % 9) + 1
          mainLevel * subLevel
        }

        def stepDeltaClick(levelDelta: Int): Unit = {
          if (level + levelDelta >= 1) {
            level += levelDelta
          }
          speedDeltaElement.find(".stepSize").value(calculateSpeedDeltaFactor.toString)
        }

        speedDeltaElement.find(".minusStep").click(() => stepDeltaClick(-1))
        speedDeltaElement.find(".plusStep").click(() => stepDeltaClick(1))

        def applySpeedChange(x: Int, y: Int): Unit = {
          speck.speed = speck.speed + Speed(MeterPerSecond(x * calculateSpeedDeltaFactor), MeterPerSecond(y * calculateSpeedDeltaFactor))
        }

        speedDeltaElement.find(".minusX").click(() => applySpeedChange(-1, 0))
        speedDeltaElement.find(".plusX").click(() => applySpeedChange(1, 0))
        speedDeltaElement.find(".minusY").click(() => applySpeedChange(0, -1))
        speedDeltaElement.find(".plusY").click(() => applySpeedChange(0, 1))

        speedDeltaElement.find(".stop").click(() => speck.speed = Speed.zero)
      }
    }

  }

  private def onToggleAnimation(): Any = {
    $(Elements.toggleAnimation.idSelector).html(
      if (animationRunning) {
        stopSimulation()
        "Start"
      } else {
        startSimulation()
        "Stop"
      }
    )
  }

}