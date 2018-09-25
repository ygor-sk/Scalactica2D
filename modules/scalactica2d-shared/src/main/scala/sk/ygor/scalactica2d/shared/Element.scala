package sk.ygor.scalactica2d.shared

case class Element(name: String) extends AnyVal {
  def idSelector = s"#$name"

  override def toString: String = name
}
