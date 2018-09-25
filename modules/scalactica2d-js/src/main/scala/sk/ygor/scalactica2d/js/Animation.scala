package sk.ygor.scalactica2d.js

import org.scalajs.dom
import org.scalajs.dom.raw.CanvasRenderingContext2D
import sk.ygor.scalactica2d.js.animation.{ObjectTreeRenderer, PixelPosition, PositionConverter}
import sk.ygor.scalactica2d.js.objects.{Marker, SpaceObject}
import sk.ygor.scalactica2d.js.scenario.Scenario
import sk.ygor.scalactica2d.js.units._
import sk.ygor.scalactica2d.js.util.Tree

/**
  * Renders scenarios to canvas context
  */
class Animation(ctx: CanvasRenderingContext2D)
  extends PositionConverter {

  private val PIXEL_PER_METER: Int = 1000
  private val centerMarker = Marker("[0,0]", Position.zero)

  private var animationRunning: Boolean = false

  private var lastAnimationRequestId: Int = 0

  private var focusedSpaceObject: SpaceObject = centerMarker
  private var focusOffset = Position.zero

  private var zoom: Zoom = Zoom(27)
  private var framesDrawn: Int = 0

  private var firstFrameDrawn: Double = 0d

  private var scenario: Scenario = _
  private var objectTreeRenderer: ObjectTreeRenderer = ObjectTreeRenderer.empty

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
    meterToPixels(position.x - focuuuus.x) + (pixelWidth / 2),
    meterToPixels(position.y - focuuuus.y) + (pixelHeight / 2)
  )

  def meterToPixels(m: Meter): Double = (m.value * PIXEL_PER_METER) / zoom.canvasZoom

  private def pixelToMeters(d: Double): Meter = Meter((d * zoom.canvasZoom) / PIXEL_PER_METER)

  private def focuuuus: Position = focusedSpaceObject.position + focusOffset

  private def draw(time: Double): Unit = {
    if (framesDrawn == 0) {
      firstFrameDrawn = time
    }
    framesDrawn += 1

    ctx.clearRect(0, 0, pixelWidth, pixelHeight)
    drawMesh()
    scenario.draw(ctx, this)
    centerMarker.draw(ctx, this)

    if (animationRunning) {
      scenario.calculateStep()
      requestAnimationFrame()
    }
  }

  def drawMesh(): Unit = {
    ctx.strokeStyle = "rgb(128,128,128)"

    // TODO: move to rescale
    // TODO: optimize
    val meshMeters = Meter(zoom.mainLevel)
    val meshOffset = focuuuus - Position(focuuuus.x % meshMeters, focuuuus.y % meshMeters)
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

  def isAnimationRunning: Boolean = animationRunning

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
    rescale()
  }

  def loadScenario(scenario: Scenario, objectTreeRenderer: ObjectTreeRenderer): Unit = {
    dom.console.log(s"loading scenario: $scenario")
    this.scenario = scenario
    this.objectTreeRenderer = objectTreeRenderer

    focusedSpaceObject = centerMarker
    focusOffset = centerMarker.position
    zoom = Zoom(27)
    rescale()

    this.objectTreeRenderer.renderObjectTree(Tree(centerMarker, Seq(scenario.spaceObjects())))
  }

  private def rescale(): Unit = {
    if (!animationRunning) {
      dom.window.requestAnimationFrame(draw)
    }
  }
}
