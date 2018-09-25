package sk.ygor.scalactica2d.js.objects

import sk.ygor.scalactica2d.js.units.{Meter, Speed}

trait CelestialObject extends SpaceObject {

  def radius: Meter

  def speed: Speed

}
