package sk.ygor.space2d.js.units

case class Meter(value: Double) extends AnyVal {

  /*@deprecated*/ def *(other: Meter): MeterSquared = MeterSquared(value * other.value)

  def +(other: Meter): Meter = Meter(value + other.value)

  def -(other: Meter): Meter = Meter(value - other.value)
}