package sk.ygor.space2d.js

import org.scalajs.dom
import org.scalajs.dom.CanvasRenderingContext2D
import org.scalajs.dom.raw.HTMLCanvasElement

class Scala2dAnimation(canvas: HTMLCanvasElement) {

  val ctx: CanvasRenderingContext2D = canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]
  val radius = 150

  private var animationRunning: Boolean = false
  private var lastAnimationRequestId: Int = 0

  private var framesDrawn: Int = 0
  private var firstFrameDrawn: Double = 0d

  private var earthX = 0d
  private var earthY = 300d

  private var earthSpeedX = 0.3d
  private var earthSpeedY = 0d

  private var framesLeft: Long = 200000000

  dom.window.setInterval(
    () => {
      dom.console.log(s"FPS: ${getAndResetFps()}")
    },
    2500
  )

  private def draw(time: Double): Unit = {
    if (framesDrawn == 0) {
      firstFrameDrawn = time
    }
    framesDrawn += 1

    ctx.clearRect(-canvas.width / 2, -canvas.height / 2, canvas.width, canvas.height)

    // sun
    ctx.fillStyle = "yellow"
    ctx.beginPath()
    ctx.arc(0, 0, 50, 0, Math.PI * 2)
    ctx.fill()

    // earth
    ctx.strokeStyle = "white"
    ctx.lineWidth = 1
    ctx.fillStyle = "grey"

    ctx.beginPath()
    ctx.arc(earthX, earthY, 10, 0, Math.PI * 2)
    ctx.stroke()
    ctx.fill()

    var cycle = 10
    while (cycle > 0) {
      val earthXMid = earthX + earthSpeedX / 2
      val earthYMid = earthY + earthSpeedY / 2

      val distanceMid = Math.sqrt(earthXMid * earthXMid + earthYMid * earthYMid)

      val gravityMid = 0.2 / (distanceMid * distanceMid)

      val earthSpeedXMid = earthSpeedX - earthXMid * gravityMid
      val earthSpeedYMid = earthSpeedY - earthYMid * gravityMid

      earthX += (earthSpeedX + earthSpeedXMid) / 2
      earthY += (earthSpeedY + earthSpeedYMid) / 2

      earthSpeedX = earthSpeedXMid
      earthSpeedY = earthSpeedYMid

      cycle -= 1
    }

    if (animationRunning) {
      framesLeft -= 1
      if (framesLeft > 0) {
        lastAnimationRequestId = dom.window.requestAnimationFrame(draw)
      }
    }
  }

  def getAndResetFps(): Double = {
    val result = framesDrawn / (dom.window.performance.now() - firstFrameDrawn) * 1000
    framesDrawn = 0
    result
  }

  def startAnimation(): Unit = {
    assert(!animationRunning)
    animationRunning = true
    lastAnimationRequestId = dom.window.requestAnimationFrame(draw)
  }

  def stopAnimation(): Unit = {
    assert(animationRunning)
    animationRunning = false
    if (lastAnimationRequestId != 0) {
      dom.window.cancelAnimationFrame(lastAnimationRequestId)
    }
  }

  def isAnimationRunning: Boolean = animationRunning

  def resizeTo(width: Double, height: Double): Any = {
    canvas.width = width.toInt
    canvas.height = height.toInt
    ctx.translate(width / 2, height / 2)
    dom.console.log(s"window resized: $width / $height")
    if (!animationRunning) {
      dom.window.requestAnimationFrame(draw)
    }
  }
}
