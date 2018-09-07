package sk.ygor.space2d.js

import org.scalajs.dom
import org.scalajs.dom.CanvasRenderingContext2D
import org.scalajs.dom.raw.HTMLCanvasElement

class Scala2dAnimation(canvas: HTMLCanvasElement) {

  val ctx: CanvasRenderingContext2D = canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]
  val radius = 150

  private var animationRunning: Boolean = false
  private var lastAnimationRequestId: Int = 0

  private var focus = Position(0, 0)
  private var zoom = 1d

  private var framesDrawn: Int = 0
  private var firstFrameDrawn: Double = 0d

  private val G = 800
  private var planet = Planet(Position(0, 250), Speed(1.1, 0))
  private val trail: Array[Position] = new Array(1000)
  private var trailIdx = 0

  dom.window.setInterval(
    () => {
      dom.console.log(s"FPS: ${getAndResetFps()}")
    },
    2500
  )


  case class Position(x: Double, y: Double) {

    def acceleration: Acceleration = {
      val distanceSquared = x * x + y * y
      val accelaration = -G / distanceSquared
      val distance = Math.sqrt(distanceSquared)
      Acceleration(x * accelaration / distance, y * accelaration / distance)
    }

    def +(speed: Speed) = Position(x + speed.x, y + speed.y)
  }

  case class Speed(x: Double, y: Double) {
    def /(d: Double) = Speed(x / d, y / d)

    def *(d: Double) = Speed(x * d, y * d)

    def +(speed: Speed) = Speed(x + speed.x, y + speed.y)

    def +(acceleration: Acceleration) = Speed(x + acceleration.x, y + acceleration.y)
  }

  case class Acceleration(x: Double, y: Double) {
    def /(d: Double) = Acceleration(x / d, y / d)

    def *(d: Double) = Acceleration(x * d, y * d)

    def +(acceleration: Acceleration) = Acceleration(x + acceleration.x, y + acceleration.y)
  }

  case class Planet(position: Position, speed: Speed)

  case class PlanetDerivative(speed: Speed, acceleration: Acceleration)

  private def draw(time: Double): Unit = {
    if (framesDrawn == 0) {
      firstFrameDrawn = time
    }
    framesDrawn += 1

    //    ctx.clearRect(-canvas.width / 2, -canvas.height / 2, canvas.width, canvas.height)

    // TODO: 10% slower
    ctx.save
    ctx.setTransform(1, 0, 0, 1, 0, 0)
    ctx.clearRect(0, 0, canvas.width, canvas.height)
    ctx.restore()

    // sun
    ctx.fillStyle = "yellow"
    ctx.beginPath()
    ctx.arc(0, 0, 50, 0, Math.PI * 2)
    ctx.fill()

    // earth
    ctx.strokeStyle = "white"
    ctx.lineWidth = 1
    ctx.fillStyle = "grey"

    ctx.beginPath()
    ctx.arc(planet.position.x, planet.position.y, 10, 0, Math.PI * 2)
    ctx.stroke()
    ctx.fill()

    ctx.fillStyle = "red"
    trail.foreach(position =>
      if (position != null) {
        ctx.fillRect(position.x, position.y, 1, 1)
      }
    )

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
      planet = nextUsingEuler(planet, precision)
      //      planet = nextUsingRangeKutta(planet, precision)
      //      planet = nextUsingMidpoint(planet, precision)
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
    val speedNew = planet.speed + (positionNew.acceleration / precision)
    Planet(positionNew, speedNew)
  }

  def nextUsingMidpoint(planet: Planet, precision: Double): Planet = {
    val positionMid = planet.position + ((planet.speed / precision) / 2)
    val speedNew = planet.speed + (positionMid.acceleration / precision)
    val positionNew = planet.position + (((planet.speed + speedNew) / 2) / precision)
    Planet(positionNew, speedNew)
  }

  def nextUsingRangeKutta(planet: Planet, precision: Double): Planet = {

    def firstDerivative: PlanetDerivative =
      PlanetDerivative(planet.speed, planet.position.acceleration)

    def nextDerivative(derivative: PlanetDerivative, step: Double): PlanetDerivative = {
      val newPosition = planet.position + derivative.speed * (step / precision)
      val newSpeed = planet.speed + derivative.acceleration * (step / precision)
      val newAcceleration = newPosition.acceleration
      PlanetDerivative(newSpeed, newAcceleration)
    }

    val a = firstDerivative
    val b = nextDerivative(a, 0.5)
    val c = nextDerivative(b, 0.5)
    val d = nextDerivative(c, 1)
    Planet(
      planet.position + ((a.speed + (b.speed + c.speed) * 2 + d.speed) / 6) / precision,
      planet.speed + ((a.acceleration + (b.acceleration + c.acceleration) * 2 + d.acceleration) / 6) / precision
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
    val zoomDelta = -wheelDelta / 1000
    if (zoom + zoomDelta > 0.1) {
      zoom += zoomDelta
    }
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
    focus = focus + Speed(deltaX, deltaY)
    rescale()
  }

  private def rescale(): Unit = {
    ctx.setTransform(1, 0, 0, 1, 0, 0)
    ctx.translate(canvas.width / 2 + focus.x, canvas.height / 2 + focus.y)
    ctx.scale(zoom, zoom)
    if (!animationRunning) {
      dom.window.requestAnimationFrame(draw)
    }
  }
}
