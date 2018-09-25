package sk.ygor.scalactica2d.js.units

case class MeterSquared(value: Double) extends AnyVal {

  def +(other: MeterSquared): MeterSquared = MeterSquared(value + other.value)

}
