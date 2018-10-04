package sk.ygor.scalactica2d.js.objects

import org.scalajs.dom.raw.CanvasRenderingContext2D
import sk.ygor.scalactica2d.js.animation.PositionConverter
import sk.ygor.scalactica2d.js.scenario.AvailableControls
import sk.ygor.scalactica2d.js.units.{Meter, Position, Speed}

case class Speck(var position: Position,
            var speed: Speed) extends MovingObject {

  override def name: String = "Speck"

  override def draw(ctx: CanvasRenderingContext2D, positionConverter: PositionConverter): Unit = {
    val pixelPosition = positionConverter.toPixelPosition(position)

    ctx.strokeStyle = "white"
    ctx.lineWidth = 1
    ctx.fillStyle = "orange"

    ctx.beginPath()
    ctx.arc(pixelPosition.x, pixelPosition.y, positionConverter.meterToPixels(Meter(1)), 0, Math.PI * 2)
    ctx.stroke()
    ctx.fill()
  }

  override def availableControls: AvailableControls = AvailableControls(speedDelta = true)
}
