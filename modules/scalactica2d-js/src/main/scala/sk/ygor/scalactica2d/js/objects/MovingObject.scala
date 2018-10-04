package sk.ygor.scalactica2d.js.objects

import sk.ygor.scalactica2d.js.units.{Position, Speed}

trait MovingObject extends SpaceObject {

  def speed: Speed

  def speed_=(newSpeed: Speed): Unit

  def position_=(newPosition: Position): Unit

}
