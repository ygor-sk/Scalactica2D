package sk.ygor.space2d.web.controller

import controllers.AssetsFinder
import play.api.mvc._
import sk.ygor.space2d.web.view

class ApplicationController(controllerComponents: ControllerComponents)
                           (implicit assetsFinder: AssetsFinder)
  extends AbstractController(controllerComponents) {

  def index = Action {
    Ok(view.html.index())
  }

}
