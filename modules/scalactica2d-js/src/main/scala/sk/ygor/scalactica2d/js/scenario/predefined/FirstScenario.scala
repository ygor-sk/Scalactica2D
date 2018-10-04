package sk.ygor.scalactica2d.js.scenario.predefined

import sk.ygor.scalactica2d.js.objects.Speck
import sk.ygor.scalactica2d.js.scenario.Mission.NextStep
import sk.ygor.scalactica2d.js.scenario.{Mission, Scenario}
import sk.ygor.scalactica2d.js.units.{Meter, MeterPerSecond, Position, Speed}
import sk.ygor.scalactica2d.js.util.SpaceObjectTree

class FirstScenario extends Scenario {

  override def name: String = "Single stationary object"

  override def mission: Mission = Mission(List(
    Mission.Step(
      """You see a simple object in space. Is it moving? Apparently not. It remains static in your screen.
        | There is no other object in the space to establish a frame of reference.
        | With one excepion - the camera. Is the camera moving?
        |""".stripMargin,
      NextStep
    ),
    Mission.Step(
      """It appears, that we cannot answer these two questions separatelly.
        | We can only ask: is the object moving relativelly to the camera? Is the camera moving relativelly to the object?
        | The answer to both questions is: No, they are not moving relativelly to eachother.
        |""".stripMargin,
      NextStep
    )
  ))

  override def createInitialSpaceObjectTree(): SpaceObjectTree = SpaceObjectTree(
    Speck(
      position = Position(Meter(15), Meter(12)),
      speed = Speed(MeterPerSecond(0.1), MeterPerSecond(0))
    )
  )

}
