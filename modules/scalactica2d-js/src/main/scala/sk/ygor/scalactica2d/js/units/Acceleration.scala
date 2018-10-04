package sk.ygor.scalactica2d.js.units

case class Acceleration(x: MeterPerSecondSquared, y: MeterPerSecondSquared) {

  def *(d: Double) = Acceleration(
    MeterPerSecondSquared(x.value * d),
    MeterPerSecondSquared(y.value * d),
  )

  def /(d: Double) = Acceleration(
    MeterPerSecondSquared(x.value / d),
    MeterPerSecondSquared(y.value / d),
  )

  def +(other: Acceleration) = Acceleration(
    x + other.x,
    y + other.y,
  )

}

object Acceleration {
  val zero: Acceleration = Acceleration(MeterPerSecondSquared(0), MeterPerSecondSquared(0))
}
