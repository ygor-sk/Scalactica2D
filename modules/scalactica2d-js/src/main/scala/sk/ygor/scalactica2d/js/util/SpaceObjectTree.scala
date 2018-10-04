package sk.ygor.scalactica2d.js.util

import sk.ygor.scalactica2d.js.objects.SpaceObject

sealed trait SpaceObjectTree {

  def allWithLevel: Seq[(SpaceObject, Int)]

  def all: Seq[SpaceObject] = allWithLevel.map { case (item, _) => item }

}

object SpaceObjectTree {
  def apply(): EmptySpaceObjectTree$.type = EmptySpaceObjectTree$

  def apply(parent: SpaceObject): SpaceObjectTree = NonEmptySpaceObjectTree(parent, Seq.empty)

  def apply(parent: SpaceObject, children: Seq[SpaceObjectTree]): SpaceObjectTree = NonEmptySpaceObjectTree(parent, children)
}

case object EmptySpaceObjectTree$ extends SpaceObjectTree {

  override def allWithLevel: Seq[Nothing] = Seq.empty
}

final case class NonEmptySpaceObjectTree(parent: SpaceObject, children: Seq[SpaceObjectTree]) extends SpaceObjectTree {

  override def allWithLevel: Seq[(SpaceObject, Int)] =
    children.foldLeft(Seq((parent, 1)))((acc, child) => acc ++ child.allWithLevel.map { case (item, level) => (item, level + 1) })
}
