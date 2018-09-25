package sk.ygor.scalactica2d.js.scenario.predefined

import sk.ygor.scalactica2d.js.objects.Planet
import sk.ygor.scalactica2d.js.scenario.Scenario
import sk.ygor.scalactica2d.js.units.{Meter, MeterPerSecond, Position, Speed}
import sk.ygor.scalactica2d.js.util.Tree

class FirstScenario extends Scenario {

  override def name: String = "Single stationary object"

  override def spaceObjects(): Tree = Tree(
    Planet(
      "Speck",
      position = Position(Meter(15), Meter(12)),
      radius = Meter(1),
      speed = Speed(MeterPerSecond(0), MeterPerSecond(0))
    )
  )

  override def calculateStep(): Unit = {
    // TODO: add speed
  }
}
