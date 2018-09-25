package sk.ygor.space2d.js.objects

import org.scalajs.dom.raw.CanvasRenderingContext2D
import sk.ygor.space2d.js.animation.PositionConverter
import sk.ygor.space2d.js.units.Position

case class Marker(name: String, position: Position) extends SpaceObject {

  override def draw(ctx: CanvasRenderingContext2D, positionConverter: PositionConverter): Unit = {
    val pixelPosition = positionConverter.toPixelPosition(position)

    ctx.strokeStyle = "green"

    ctx.beginPath()
    ctx.moveTo(pixelPosition.x - 20, pixelPosition.y)
    ctx.lineTo(pixelPosition.x + 20, pixelPosition.y)
    ctx.moveTo(pixelPosition.x, pixelPosition.y - 20)
    ctx.lineTo(pixelPosition.x, pixelPosition.y + 20)
    ctx.stroke()

    ctx.font = "24px Arial"
    ctx.fillStyle = "green"
    ctx.fillText(name, pixelPosition.x + 10, pixelPosition.y - 10)
  }
}
