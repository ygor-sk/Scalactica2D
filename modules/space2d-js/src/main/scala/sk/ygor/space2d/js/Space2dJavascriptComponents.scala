package sk.ygor.space2d.js

import com.softwaremill.macwire.wire
import org.scalajs.dom.CanvasRenderingContext2D
import org.scalajs.dom.raw.HTMLCanvasElement
import sk.ygor.space2d.js.scenario.ScenarioListService

trait Space2dJavascriptComponents {

  def canvas: HTMLCanvasElement

  def ctx: CanvasRenderingContext2D

  lazy val userInterface: UserInterface = wire[UserInterface]
  lazy val animation: Animation = wire[Animation]
  lazy val scenarioListService: ScenarioListService = wire[ScenarioListService]
  lazy val ajaxClient: AjaxClient = wire[AjaxClient]

}
