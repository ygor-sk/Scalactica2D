package sk.ygor.space2d.js

import org.scalajs.dom
import org.scalajs.dom.CanvasRenderingContext2D
import org.scalajs.dom.raw.HTMLCanvasElement
import sk.ygor.space2d.js.units._

class Scala2dAnimation(canvas: HTMLCanvasElement) {

  val ctx: CanvasRenderingContext2D = canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]
  val radius = 150

  private var animationRunning: Boolean = false
  private var lastAnimationRequestId: Int = 0

  private var focus = Position(Meter(0), Meter(0))
  private var zoom: Int = 10

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

  case class CanvasPosition(x: Double, y: Double)

  case class Planet(position: Position, radius: Meter, speed: Speed)

  case class PlanetDerivative(speed: Speed, acceleration: Acceleration)

  private def toCanvasPosition(position: Position): CanvasPosition = CanvasPosition(
    toCanvasDistance(position.x - focuuuus.x) + (canvas.width / 2),
    toCanvasDistance(position.y - focuuuus.y) + (canvas.height / 2)
  )

  private def toCanvasDistance(m: Meter): Double = m.value * zooooom

  private def focuuuus: Position = focus

  private def draw(time: Double): Unit = {
    if (framesDrawn == 0) {
      firstFrameDrawn = time
    }
    framesDrawn += 1

    ctx.clearRect(0, 0, canvas.width, canvas.height)

    // sun
    val sunCanvasPosition = toCanvasPosition(Position(Meter(0), Meter(0)))
    ctx.fillStyle = "yellow"
    ctx.beginPath()
    ctx.arc(sunCanvasPosition.x, sunCanvasPosition.y, toCanvasDistance(Meter(50)), 0, Math.PI * 2)
    ctx.fill()

    // earth
    val planetCanvasPosition = toCanvasPosition(planet.position)

    ctx.strokeStyle = "white"
    ctx.lineWidth = 1
    ctx.fillStyle = "grey"

    ctx.beginPath()
    ctx.arc(planetCanvasPosition.x, planetCanvasPosition.y, toCanvasDistance(planet.radius), 0, Math.PI * 2)
    ctx.stroke()
    ctx.fill()

    ctx.fillStyle = "red"
    trail.foreach(position =>
      if (position != null) {
        val trailCanvasPosition = toCanvasPosition(position)
        ctx.fillRect(trailCanvasPosition.x, trailCanvasPosition.y, 1, 1)
      }
    )

    //     draw mesh
    //    val meshSize = ((1 - (zoom % 1)) * 10).toInt
    //    for (int x =                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 )

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
    val zoomDelta = (-wheelDelta / 100).toInt
    zoom = Math.max(zoom + zoomDelta, 0)
    dom.console.log(s"zoom set to: $zoom")
    rescale()
  }

  /**
    * @return 1, 2, 3, .., 9, 10, 20, 30, ..., 90, 100, 200, 300, ..., 900, ...
    */
  private def zooooom: Double = {
    val a = zoom / 9
    val b = zoom % 9
    val result = Math.pow(10, a) * (b + 1)
    //    val result = 1 / Math.log10(zoom)
    dom.console.log(s"zoom=$zoom, a=$a, b=$b, result=$result")
    result

  }

  def isAnimationRunning: Boolean = animationRunning

  def resizeTo(width: Double, height: Double): Unit = {
    canvas.width = width.toInt
    canvas.height = height.toInt
    rescale()
    dom.console.log(s"window resized: $width / $height")
  }

  def dragBy(deltaX: Double, deltaY: Double): Unit = {
    focus = focus - Position(Meter(deltaX / zooooom), Meter(deltaY / zooooom))
    rescale()
  }

  private def rescale(): Unit = {
    if (!animationRunning) {
      dom.window.requestAnimationFrame(draw)
    }
  }
}
