package com.github.oen9.deeprps.gui.geometry

import scalafx.beans.property.DoubleProperty
import scalafx.geometry.Rectangle2D
import scalafx.scene.Node

class Rect(
  _x: Double,
  _y: Double,
  _width: Double,
  _height: Double
) {
  val x = DoubleProperty(_x)
  val y = DoubleProperty(_y)
  val width = DoubleProperty(_width)
  val height = DoubleProperty(_height)

  def to2D() = new Rectangle2D(x(), y(), width(), height())
  def to2DWithShift(node: Node) = new Rectangle2D(
    x() + node.layoutX(),
    y() + node.layoutY(),
    width(),
    height()
  )
}
