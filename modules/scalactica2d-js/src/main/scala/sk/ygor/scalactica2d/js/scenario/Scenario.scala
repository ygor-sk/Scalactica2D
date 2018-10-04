package sk.ygor.scalactica2d.js.scenario

import sk.ygor.scalactica2d.js.util.SpaceObjectTree

trait Scenario {

  def name: String

  def mission: Mission = Mission(List(
    Mission.Step("TODO: step 1", Mission.NextStep),
    Mission.Step("TODO: step 2", Mission.NextStep),
    Mission.Step("TODO: step 3", Mission.Condition("TODO: add condition")),
  ))

  def createInitialSpaceObjectTree(): SpaceObjectTree

}
