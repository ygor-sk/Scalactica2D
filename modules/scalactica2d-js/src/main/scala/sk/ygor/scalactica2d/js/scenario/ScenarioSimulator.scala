package sk.ygor.scalactica2d.js.scenario

import sk.ygor.scalactica2d.js.objects.{MovingObject, SpaceObject, Sun}
import sk.ygor.scalactica2d.js.units.{Acceleration, MeterPerSecondSquared, Position}
import sk.ygor.scalactica2d.js.util.SpaceObjectTree

class ScenarioSimulator(scenario: Scenario, eventListener: ScenarioSimulator.EventListener) {

  private final val G = 800

  val currentTree: SpaceObjectTree = scenario.createInitialSpaceObjectTree()

  // derivatives
  private val movingObjects = currentTree.all.collect { case movingObject: MovingObject => movingObject }.toArray
  private val suns = currentTree.all.collect { case sun: Sun => sun }.toArray
  private val all = currentTree.all.toArray

  def allSpaceObjects(): Array[SpaceObject] = all

  def calculateNextStep(): Unit = {
    val precision = 10
    var cycle = precision

    while (cycle > 0) {
      nextUsingMidpoint(precision)
      cycle -= 1
    }
  }

  //  def calculateStep(): Unit = {
  //    trail(trailIdx) = planet.position
  //    trailIdx += 1
  //    if (trailIdx == trail.length) {
  //      trailIdx = 0
  //    }
  //  }

  //  def nextUsingEuler(planet: Planet, precision: Double): Unit = {
  //    val positionNew = planet.position + (planet.speed / precision)
  //    val accelerationNew = acceleration(sun.position, planet.position)
  //    val speedNew = planet.speed + (accelerationNew / precision)
  //    planet.copy(position = positionNew, speed = speedNew)
  //  }

  def nextUsingMidpoint(precision: Double): Unit = {
    for (i <- movingObjects.indices) {
      val movingObject = movingObjects(i)
      val positionMid = movingObject.position + ((movingObject.speed / precision) / 2)
      var acceleration = Acceleration.zero
      for (j <- suns.indices) {
        val sun = suns(j)
        val accelerationMid = calculationAcceleration(sun.position, positionMid)
        acceleration = acceleration + accelerationMid
      }

      val speedNew = movingObject.speed + (acceleration / precision)
      val positionNew = movingObject.position + (((movingObject.speed + speedNew) / 2) / precision)
      movingObject.position = positionNew
      movingObject.speed = speedNew

      // TODO: optimize - only notifiy on real change
      eventListener.objectPositionChanged(movingObject)
      eventListener.objectSpeedChanged(movingObject)
      eventListener.objectAccelerationChanged(movingObject, acceleration)
    }
  }

  //  def nextUsingRangeKutta(planet: Planet, precision: Double): Planet = {
  //
  //    def firstDerivative: PlanetDerivative =
  //      PlanetDerivative(planet.speed, acceleration(sun.position, planet.position))
  //
  //    def nextDerivative(derivative: PlanetDerivative, step: Double): PlanetDerivative = {
  //      val newPosition = planet.position + derivative.speed * (step / precision)
  //      val newSpeed = planet.speed + derivative.acceleration * (step / precision)
  //      val newAcceleration = acceleration(sun.position, newPosition)
  //      PlanetDerivative(newSpeed, newAcceleration)
  //    }
  //
  //    val a = firstDerivative
  //    val b = nextDerivative(a, 0.5)
  //    val c = nextDerivative(b, 0.5)
  //    val d = nextDerivative(c, 1)
  //    planet.copy(
  //      position = planet.position + ((a.speed + (b.speed + c.speed) * 2 + d.speed) / 6) / precision,
  //      speed = planet.speed + ((a.acceleration + (b.acceleration + c.acceleration) * 2 + d.acceleration) / 6) / precision
  //    )
  //  }

  def calculationAcceleration(centerPosition: Position, orbitingPosition: Position): Acceleration = {
    val relativePosition = orbitingPosition - centerPosition
    val distanceSquared = relativePosition.x * relativePosition.x + relativePosition.y * relativePosition.y
    val accelaration: Double = -G / distanceSquared.value
    val distance = Math.sqrt(distanceSquared.value)
    Acceleration(
      MeterPerSecondSquared(relativePosition.x.value * accelaration / distance),
      MeterPerSecondSquared(relativePosition.y.value * accelaration / distance),
    )
  }

}

object ScenarioSimulator {

  trait EventListener {
    def objectPositionChanged(spaceObject: SpaceObject): Unit

    def objectSpeedChanged(movingObject: MovingObject): Unit

    def objectAccelerationChanged(spaceObject: SpaceObject, acceleration: Acceleration): Unit
  }

}