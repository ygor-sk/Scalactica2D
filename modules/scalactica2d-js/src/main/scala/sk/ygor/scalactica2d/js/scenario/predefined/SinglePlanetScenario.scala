package sk.ygor.scalactica2d.js.scenario.predefined

import sk.ygor.scalactica2d.js.objects._
import sk.ygor.scalactica2d.js.scenario.Scenario
import sk.ygor.scalactica2d.js.units._
import sk.ygor.scalactica2d.js.util.SpaceObjectTree

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

  override def createInitialSpaceObjectTree(): SpaceObjectTree = SpaceObjectTree(sun, Seq(SpaceObjectTree(planet)))
}
