package sk.ygor.space2d.js.objects

import org.scalajs.dom.raw.CanvasRenderingContext2D
import sk.ygor.space2d.js.animation.PositionConverter
import sk.ygor.space2d.js.units.{Meter, Position, Speed}

case class Sun(position: Position, radius: Meter, speed: Speed) extends CelestialObject {

  val name: String = "Sun"

  override def draw(ctx: CanvasRenderingContext2D, positionConverter: PositionConverter): Unit = {
    val sunCanvasPosition = positionConverter.toPixelPosition(position)
    ctx.fillStyle = "yellow"
    ctx.beginPath()
    ctx.arc(sunCanvasPosition.x, sunCanvasPosition.y, positionConverter.meterToPixels(radius), 0, Math.PI * 2)
    ctx.fill()

    ctx.fillStyle = "red"
    ctx.beginPath()
    val canvasMeter = positionConverter.meterToPixels(Meter(1))
    ctx.rect(sunCanvasPosition.x - canvasMeter / 2, sunCanvasPosition.y - canvasMeter / 2, canvasMeter, canvasMeter)
    ctx.fill()
  }

}