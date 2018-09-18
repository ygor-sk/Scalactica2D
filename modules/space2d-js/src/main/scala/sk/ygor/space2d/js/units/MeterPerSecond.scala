package sk.ygor.space2d.js.units

case class MeterPerSecond(value: Double) extends AnyVal {

  def +(other: MeterPerSecond): MeterPerSecond = MeterPerSecond(value + other.value)

}
