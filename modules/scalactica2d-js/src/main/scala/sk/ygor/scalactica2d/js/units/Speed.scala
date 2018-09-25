package sk.ygor.scalactica2d.js.units

case class Speed(x: MeterPerSecond, y: MeterPerSecond) {

  def *(d: Double): Speed = Speed(
    MeterPerSecond(x.value * d),
    MeterPerSecond(y.value * d)
  )

  def /(d: Double): Speed = Speed(
    MeterPerSecond(x.value / d),
    MeterPerSecond(y.value / d)
  )

  def +(other: Speed): Speed = Speed(
    x + MeterPerSecond(other.x.value),
    y + MeterPerSecond(other.y.value),
  )

  def +(acceleration: Acceleration): Speed = Speed(
    x + MeterPerSecond(acceleration.x.value),
    y + MeterPerSecond(acceleration.y.value)
  )

}
