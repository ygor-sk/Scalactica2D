package sk.ygor.scalactica2d.js

import com.softwaremill.macwire.wire
import org.scalajs.dom.raw.HTMLCanvasElement
import sk.ygor.scalactica2d.js.scenario.{Scenario, ScenarioListService, ScenarioSimulator}

trait Scalactica2dJavascriptComponents {

  def canvas: HTMLCanvasElement

  lazy val userInterface: UserInterface = wire[UserInterface]
  lazy val scenarioListService: ScenarioListService = wire[ScenarioListService]
  lazy val ajaxClient: AjaxClient = wire[AjaxClient]
  lazy val scenarioSimulatorFactory: ScenarioSimulatorFactory = (_: Scenario, _: ScenarioSimulator.EventListener) => wire[ScenarioSimulator]

}
