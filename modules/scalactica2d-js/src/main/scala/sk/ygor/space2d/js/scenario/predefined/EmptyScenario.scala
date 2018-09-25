package sk.ygor.space2d.js.scenario.predefined

import sk.ygor.space2d.js.scenario.Scenario
import sk.ygor.space2d.js.util.Tree

class EmptyScenario extends Scenario {

  override def name: String = "Empty"

  override def spaceObjects(): Tree = Tree()

  override def calculateStep(): Unit = {
    // do nothing
  }
}
