package sk.ygor.scalactica2d.js.objects

import org.scalajs.dom.raw.CanvasRenderingContext2D
import sk.ygor.scalactica2d.js.animation.PositionConverter
import sk.ygor.scalactica2d.js.units.Position

trait SpaceObject {

  def name: String

  def position: Position

  def draw(ctx: CanvasRenderingContext2D, positionConverter: PositionConverter): Unit

  val id: Int = {
    SpaceObject.counter += 1
    SpaceObject.counter
  }

}

object SpaceObject {
  var counter: Int = 1000
}
