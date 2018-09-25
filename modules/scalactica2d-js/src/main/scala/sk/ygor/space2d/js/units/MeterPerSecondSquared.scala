package sk.ygor.space2d.js.units

case class MeterPerSecondSquared(value: Double) extends AnyVal {

  def +(other: MeterPerSecondSquared): MeterPerSecondSquared = MeterPerSecondSquared(value + other.value)

}
