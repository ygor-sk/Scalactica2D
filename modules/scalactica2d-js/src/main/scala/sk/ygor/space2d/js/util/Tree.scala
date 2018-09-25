package sk.ygor.space2d.js.util

import sk.ygor.space2d.js.objects.SpaceObject

sealed trait Tree {
  def allWithLevel: Seq[(SpaceObject, Int)]

  def all: Seq[SpaceObject] = allWithLevel.map { case (item, _) => item }

}

object Tree {
  def apply(): EmptyTree.type = EmptyTree

  def apply(parent: SpaceObject): Tree = NonEmptyTree(parent, Seq.empty)

  def apply(parent: SpaceObject, children: Seq[Tree]): Tree = NonEmptyTree(parent, children)
}

case object EmptyTree extends Tree {

  override def allWithLevel: Seq[Nothing] = Seq.empty
}

final case class NonEmptyTree(parent: SpaceObject, children: Seq[Tree]) extends Tree {

  override def allWithLevel: Seq[(SpaceObject, Int)] =
    children.foldLeft(Seq((parent, 1)))((acc, child) => acc ++ child.allWithLevel.map { case (item, level) => (item, level + 1) })
}
