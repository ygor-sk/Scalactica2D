package sk.ygor.space2d.js

import org.scalajs.dom
import org.scalajs.dom.CanvasRenderingContext2D
import org.scalajs.dom.raw.HTMLCanvasElement
import sk.ygor.space2d.`macro`.DebugMacro
import sk.ygor.space2d.js.units._

class Scala2dAnimation(canvas: HTMLCanvasElement) {

  private val PIXEL_PER_METER: Int = 1000

  private val ctx: CanvasRenderingContext2D = canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]

  private var animationRunning: Boolean = false
  private var lastAnimationRequestId: Int = 0

  private var focus = Position(Meter(0), Meter(0))
  private var zoom: Zoom = Zoom(27)

  private var framesDrawn: Int = 0
  private var firstFrameDrawn: Double = 0d

  private val G = 800

  private val sun = Planet(
    Position(Meter(0), Meter(0)),
    Meter(50),
    Speed(MeterPerSecond(1.1), MeterPerSecond(0))
  )

  private var planet = Planet(
    Position(Meter(0), Meter(250)),
    Meter(10),
    Speed(MeterPerSecond(1.1), MeterPerSecond(0))
  )

  private val trail: Array[Position] = new Array(1000)
  private var trailIdx = 0

  dom.window.setInterval(
    () => {
      dom.console.log(s"FPS: ${getAndResetFps()}")
    },
    2500
  )

  case class PixelPosition(x: Double, y: Double)

  case class Planet(position: Position, radius: Meter, speed: Speed)

  case class PlanetDerivative(speed: Speed, acceleration: Acceleration)

  case class Zoom(level: Int) {

    val mainLevel: Int = Math.pow(10, level / 9).toInt
    val subLevel: Int = (level % 9) + 1

    /**
      * 1, 2, 3, .., 9, 10, 20, 30, ..., 90, 100, 200, 300, ..., 900, ...
      */
    val canvasZoom: Int = mainLevel * subLevel
  }

  private def toPixelPosition(position: Position): PixelPosition = PixelPosition(
    meterToPixels(position.x - focuuuus.x) + (canvas.width / 2),
    meterToPixels(position.y - focuuuus.y) + (canvas.height / 2)
  )

  private def meterToPixels(m: Meter): Double = (m.value * PIXEL_PER_METER) / zoom.canvasZoom

  private def pixelToMeters(d: Double): Meter = Meter((d * zoom.canvasZoom) / PIXEL_PER_METER)


  private def focuuuus: Position = planet.position

  private def draw(time: Double): Unit = {

    def drawEarth(): Unit = {
      val planetCanvasPosition = toPixelPosition(planet.position)

      ctx.strokeStyle = "white"
      ctx.lineWidth = 1
      ctx.fillStyle = "grey"

      ctx.beginPath()
      ctx.arc(planetCanvasPosition.x, planetCanvasPosition.y, meterToPixels(planet.radius), 0, Math.PI * 2)
      ctx.stroke()
      ctx.fill()
    }

    def drawEarthTrail(): Unit = {
      ctx.strokeStyle = "red"
      ctx.beginPath()
      var trailIdx = 0
      while (trailIdx < trail.length) {
        val position = trail(trailIdx)
        if (position != null) {
          val trailCanvasPosition = toPixelPosition(position)
          ctx.rect(trailCanvasPosition.x, trailCanvasPosition.y, 1, 1)
          ctx.stroke()
        }
        trailIdx += 1
      }
    }

    def drawSun(): Unit = {
      val sunCanvasPosition = toPixelPosition(sun.position)
      ctx.fillStyle = "yellow"
      ctx.beginPath()
      ctx.arc(sunCanvasPosition.x, sunCanvasPosition.y, meterToPixels(Meter(50)), 0, Math.PI * 2)
      ctx.fill()

      ctx.fillStyle = "red"
      ctx.beginPath()
      val canvasMeter = meterToPixels(Meter(1))
      ctx.rect(sunCanvasPosition.x - canvasMeter / 2, sunCanvasPosition.y - canvasMeter / 2, canvasMeter, canvasMeter)
      ctx.fill()
    }

    def drawMesh(): Unit = {
      ctx.strokeStyle = "rgb(128,128,128)"

      // TODO: move to rescale
      // TODO: optimize
      val meshMeters = Meter(zoom.mainLevel)
      val meshOffset = focuuuus - Position(focuuuus.x % meshMeters, focuuuus.y % meshMeters)
      val meshPixels = meterToPixels(meshMeters)
      val meshCountX = (canvas.width / meshPixels / 2).toInt + 1
      val meshCountY = (canvas.height / meshPixels / 2).toInt + 1
      val meshOffsetPixels = toPixelPosition(meshOffset)

      dom.console.log(DebugMacro.debugParameters("Mesh", meshMeters, meshOffset, meshPixels, meshCountX, meshCountY, meshOffsetPixels))

      for (x <- 0 to meshCountX) {
        ctx.beginPath()
        val dx = (meshOffsetPixels.x + x * meshPixels).toInt
        ctx.moveTo(dx, 0)
        ctx.lineTo(dx, canvas.height)
        if (x > 0) {
          val dx2 = (meshOffsetPixels.x - x * meshPixels).toInt
          ctx.moveTo(dx2, 0)
          ctx.lineTo(dx2, canvas.height)
        }
        ctx.closePath()
        ctx.stroke()
      }

      for (y <- 0 to meshCountY) {
        ctx.beginPath()
        val dy = (meshOffsetPixels.y + y * meshPixels).toInt
        ctx.moveTo(0, dy)
        ctx.lineTo(canvas.width, dy)
        if (y > 0) {
          val dy2 = (meshOffsetPixels.y - y * meshPixels).toInt
          ctx.moveTo(0, dy2)
          ctx.lineTo(canvas.width, dy2)
        }
        ctx.closePath()
        ctx.stroke()
      }
    }

    if (framesDrawn == 0) {
      firstFrameDrawn = time
    }
    framesDrawn += 1

    ctx.clearRect(0, 0, canvas.width, canvas.height)

    drawMesh()
    drawSun()
    drawEarth()
    drawEarthTrail()

    if (animationRunning) {
      calculateStep()
      requestAnimationFrame()
    }
  }

  private def requestAnimationFrame(): Unit = {
    lastAnimationRequestId = dom.window.requestAnimationFrame(draw)
  }

  private def calculateStep(): Unit = {
    val precision = 10
    var cycle = precision
    while (cycle > 0) {
      //      planet = nextUsingEuler(planet, precision)
      planet = nextUsingRangeKutta(planet, precision)
      //                  planet = nextUsingMidpoint(planet, precision)
      cycle -= 1
    }
    trail(trailIdx) = planet.position
    trailIdx += 1
    if (trailIdx == trail.length) {
      trailIdx = 0
    }
  }

  def nextUsingEuler(planet: Planet, precision: Double): Planet = {
    val positionNew = planet.position + (planet.speed / precision)
    val accelerationNew = acceleration(sun.position, planet.position)
    val speedNew = planet.speed + (accelerationNew / precision)
    planet.copy(position = positionNew, speed = speedNew)
  }

  def nextUsingMidpoint(planet: Planet, precision: Double): Planet = {
    val positionMid = planet.position + ((planet.speed / precision) / 2)
    val accelerationMid = acceleration(sun.position, positionMid)
    val speedNew = planet.speed + (accelerationMid / precision)
    val positionNew = planet.position + (((planet.speed + speedNew) / 2) / precision)
    planet.copy(position = positionNew, speed = speedNew)
  }

  def nextUsingRangeKutta(planet: Planet, precision: Double): Planet = {

    def firstDerivative: PlanetDerivative =
      PlanetDerivative(planet.speed, acceleration(sun.position, planet.position))

    def nextDerivative(derivative: PlanetDerivative, step: Double): PlanetDerivative = {
      val newPosition = planet.position + derivative.speed * (step / precision)
      val newSpeed = planet.speed + derivative.acceleration * (step / precision)
      val newAcceleration = acceleration(sun.position, newPosition)
      PlanetDerivative(newSpeed, newAcceleration)
    }

    val a = firstDerivative
    val b = nextDerivative(a, 0.5)
    val c = nextDerivative(b, 0.5)
    val d = nextDerivative(c, 1)
    planet.copy(
      position = planet.position + ((a.speed + (b.speed + c.speed) * 2 + d.speed) / 6) / precision,
      speed = planet.speed + ((a.acceleration + (b.acceleration + c.acceleration) * 2 + d.acceleration) / 6) / precision
    )
  }

  def acceleration(centerPosition: Position, orbitingPosition: Position): Acceleration = {
    val relativePosition = orbitingPosition - centerPosition
    val distanceSquared = relativePosition.x * relativePosition.x + relativePosition.y * relativePosition.y
    val accelaration: Double = -G / distanceSquared.value
    val distance = Math.sqrt(distanceSquared.value)
    Acceleration(
      MeterPerSecondSquared(relativePosition.x.value * accelaration / distance),
      MeterPerSecondSquared(relativePosition.y.value * accelaration / distance),
    )
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

  def resizeTo(width: Double, height: Double): Unit = {
    canvas.width = width.toInt
    canvas.height = height.toInt
    rescale()
    dom.console.log(s"window resized: $width / $height")
  }

  def dragBy(deltaX: Double, deltaY: Double): Unit = {
    focus = focus - Position(pixelToMeters(deltaX), pixelToMeters(deltaY))
    rescale()
  }

  private def rescale(): Unit = {
    if (!animationRunning) {
      dom.window.requestAnimationFrame(draw)
    }
  }
}
