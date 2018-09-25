package sk.ygor.scalactica2d.js.animation

import sk.ygor.scalactica2d.js.util.Tree

trait ObjectTreeRenderer {

  def renderObjectTree(tree: Tree): Unit

}

object ObjectTreeRenderer {
  def empty: ObjectTreeRenderer = (_: Tree) => Unit // do nothing
}
