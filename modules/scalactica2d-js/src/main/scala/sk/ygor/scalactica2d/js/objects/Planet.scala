package sk.ygor.scalactica2d.js.objects

import org.scalajs.dom.raw.CanvasRenderingContext2D
import sk.ygor.scalactica2d.js.animation.PositionConverter
import sk.ygor.scalactica2d.js.units.{Meter, Position, Speed}

case class Planet(name: String,
                  var position: Position,
                  radius: Meter,
                  var speed: Speed) extends MovingObject {

  override def draw(ctx: CanvasRenderingContext2D, positionConverter: PositionConverter): Unit = {
    val planetCanvasPosition = positionConverter.toPixelPosition(position)

    ctx.strokeStyle = "white"
    ctx.lineWidth = 1
    ctx.fillStyle = "grey"

    ctx.beginPath()
    ctx.arc(planetCanvasPosition.x, planetCanvasPosition.y, positionConverter.meterToPixels(radius), 0, Math.PI * 2)
    ctx.stroke()
    ctx.fill()
  }

}
