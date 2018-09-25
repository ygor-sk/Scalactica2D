package sk.ygor.scalactica2d.js.scenario

import org.scalajs.dom.raw.CanvasRenderingContext2D
import sk.ygor.scalactica2d.js.animation.PositionConverter
import sk.ygor.scalactica2d.js.util.Tree

trait Scenario {

  def name: String

  def spaceObjects(): Tree

  def calculateStep(): Unit

  def draw(ctx: CanvasRenderingContext2D, positionConverter: PositionConverter): Unit = {
    spaceObjects().all.foreach(spaceObject => spaceObject.draw(ctx, positionConverter))
  }


}
