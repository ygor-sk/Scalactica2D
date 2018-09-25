package sk.ygor.space2d.web

import com.softwaremill.macwire.wire
import controllers.AssetsFinder
import play.api.mvc.ControllerComponents
import sk.ygor.space2d.web.controller.ApplicationController

trait Space2dWebComponents {

  def controllerComponents: ControllerComponents

  implicit def assetsFinder: AssetsFinder

  lazy val applicationController: ApplicationController = wire[ApplicationController]

}
