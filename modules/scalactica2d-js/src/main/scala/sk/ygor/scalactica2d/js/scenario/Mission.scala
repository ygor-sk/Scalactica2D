package sk.ygor.scalactica2d.js.scenario

case class Mission(steps: List[Mission.Step])

object Mission {

  case class Step(description: String, advance: Advance)

  sealed trait Advance

  case object NextStep extends Advance

  case class Condition(description: String) extends Advance

}
