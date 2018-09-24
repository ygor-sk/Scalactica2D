package sk.ygor.space2d.js.animation

import sk.ygor.space2d.js.units.{Meter, Position}

trait PositionConverter {

  def meterToPixels(radius: Meter): Double

  def toPixelPosition(position: Position): PixelPosition

}
