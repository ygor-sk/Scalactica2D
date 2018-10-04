package sk.ygor.scalactica2d

import sk.ygor.scalactica2d.js.scenario.{Scenario, ScenarioSimulator}

package object js {
  type ScenarioSimulatorFactory = (Scenario, ScenarioSimulator.EventListener) => ScenarioSimulator
}
