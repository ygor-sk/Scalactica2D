package sk.ygor.scalactica2d.js.scenario

import sk.ygor.scalactica2d.js.scenario.predefined.{EmptyScenario, FirstScenario, SinglePlanetScenario}

class ScenarioListService {

  def availableScenarios: Seq[Scenario] = Seq(
    new EmptyScenario,
    new FirstScenario,
    new SinglePlanetScenario,
  )

}
