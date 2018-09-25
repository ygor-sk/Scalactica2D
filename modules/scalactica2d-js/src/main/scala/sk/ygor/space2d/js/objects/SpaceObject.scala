package sk.ygor.space2d.js.objects

import org.scalajs.dom.raw.CanvasRenderingContext2D
import sk.ygor.space2d.js.animation.PositionConverter
import sk.ygor.space2d.js.units.Position

trait SpaceObject {

  def name: String

  def position: Position

  def draw(ctx: CanvasRenderingContext2D, positionConverter: PositionConverter): Unit

}
