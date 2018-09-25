package sk.ygor.scalactica2d.js.scenario.predefined

import sk.ygor.scalactica2d.js.scenario.Scenario
import sk.ygor.scalactica2d.js.util.Tree

class EmptyScenario extends Scenario {

  override def name: String = "Empty"

  override def spaceObjects(): Tree = Tree()

  override def calculateStep(): Unit = {
    // do nothing
  }
}
