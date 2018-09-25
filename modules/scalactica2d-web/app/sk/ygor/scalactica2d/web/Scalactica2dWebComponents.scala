package sk.ygor.scalactica2d.web

import com.softwaremill.macwire.wire
import controllers.AssetsFinder
import play.api.mvc.ControllerComponents
import sk.ygor.scalactica2d.web.controller.ApplicationController

trait Scalactica2dWebComponents {

  def controllerComponents: ControllerComponents

  implicit def assetsFinder: AssetsFinder

  lazy val applicationController: ApplicationController = wire[ApplicationController]

}
