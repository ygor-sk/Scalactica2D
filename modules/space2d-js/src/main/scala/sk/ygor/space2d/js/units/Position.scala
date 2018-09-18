package sk.ygor.space2d.js.units

case class Position(x: Meter, y: Meter) {

  def +(distance: Position): Position = Position(Meter(x.value + distance.x.value), Meter(y.value + distance.y.value))

  def -(distance: Position): Position = this + distance.negative

  def negative: Position = Position(Meter(-x.value), Meter(-y.value))

  def +(speed: Speed) = Position(x + Meter(speed.x.value), y + Meter(speed.y.value))


}
