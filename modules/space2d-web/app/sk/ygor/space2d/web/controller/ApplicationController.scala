package sk.ygor.space2d.web.controller

import controllers.AssetsFinder
import play.api.mvc._
import sk.ygor.space2d.`macro`.DebugMacro
import sk.ygor.space2d.web.view


class ApplicationController(controllerComponents: ControllerComponents)
                           (implicit assetsFinder: AssetsFinder)
  extends AbstractController(controllerComponents) {

  def index = Action {
    val z = 15.3
    val x = "ghghdsfsdff"
    println(DebugMacro.debugParameters(z, x))
    Ok(view.html.index())
  }

}
