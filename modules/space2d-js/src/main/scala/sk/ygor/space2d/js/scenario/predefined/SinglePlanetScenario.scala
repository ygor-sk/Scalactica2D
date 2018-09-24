package sk.ygor.space2d.js.scenario.predefined

import sk.ygor.space2d.js.objects._
import sk.ygor.space2d.js.scenario.Scenario
import sk.ygor.space2d.js.units._
import sk.ygor.space2d.js.util.Tree

class SinglePlanetScenario extends Scenario {

  private val sun = Sun(
    Position.zero,
    Meter(50),
    Speed(MeterPerSecond(1.1), MeterPerSecond(0))
  )

  private val planet = Planet(
    "Earth",
    Position(Meter(0), Meter(250)),
    Meter(10),
    Speed(MeterPerSecond(1.1), MeterPerSecond(0))
  )

  //  private val trail: Array[Position] = new Array(1000)
  //  private var trailIdx = 0

  private val G = 800

  //  def drawEarthTrail(): Unit = {
  //    ctx.strokeStyle = "red"
  //    ctx.beginPath()
  //    var trailIdx = 0
  //    while (trailIdx < trail.length) {
  //      val position = trail(trailIdx)
  //      if (position != null) {
  //        val trailCanvasPosition = positionConverter.toPixelPosition(position)
  //        ctx.rect(trailCanvasPosition.x, trailCanvasPosition.y, 1, 1)
  //        ctx.stroke()
  //      }
  //      trailIdx += 1
  //    }
  //  }


  override def name: String = "Single planet around sun"

  override def calculateStep(): Unit = {
    val precision = 10
    var cycle = precision
    while (cycle > 0) {
      //      planet = nextUsingEuler(planet, precision)
//     planet = nextUsingRangeKutta(planet, precision)
               nextUsingMidpoint(planet, precision)
      cycle -= 1
    }
    //    trail(trailIdx) = planet.position
    //    trailIdx += 1
    //    if (trailIdx == trail.length) {
    //      trailIdx = 0
    //    }
  }

  def nextUsingEuler(planet: Planet, precision: Double): Unit = {
    val positionNew = planet.position + (planet.speed / precision)
    val accelerationNew = acceleration(sun.position, planet.position)
    val speedNew = planet.speed + (accelerationNew / precision)
    planet.copy(position = positionNew, speed = speedNew)
  }

  def nextUsingMidpoint(planet: Planet, precision: Double): Unit = {
    val positionMid = planet.position + ((planet.speed / precision) / 2)
    val accelerationMid = acceleration(sun.position, positionMid)
    val speedNew = planet.speed + (accelerationMid / precision)
    val positionNew = planet.position + (((planet.speed + speedNew) / 2) / precision)
    planet.position = positionNew
    planet.speed = speedNew
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

  override def spaceObjects(): Tree = Tree(sun, Seq(Tree(planet)))
}
