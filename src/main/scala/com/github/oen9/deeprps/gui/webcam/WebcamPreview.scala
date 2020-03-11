package com.github.oen9.deeprps.gui.webcam

import scalafx.Includes._
import scalafx.scene.canvas.Canvas
import scalafx.scene.input.MouseEvent
import com.github.oen9.deeprps.gui.geometry.Rect
import scalafx.scene.canvas.GraphicsContext
import scalafx.scene.paint.Color
import scalafx.scene.image.Image
import scalafx.beans.property.ObjectProperty

object WebcamPreview {
  def apply(width: Double, height: Double, selectionRect: Rect, baseImg: ObjectProperty[Image]): Canvas = new Canvas(width = width, height = height) {
    baseImg.onChange(drawWebcamPreview(graphicsContext2D))
    handleEvent(MouseEvent.Any) {
      me: MouseEvent => {
        me.eventType match {
          case MouseEvent.MousePressed =>
            selectionRect.x() = me.x
            selectionRect.y() = me.y
            selectionRect.width() = 0
            selectionRect.height() = 0
          case MouseEvent.MouseDragged =>
            val newWidth = me.x - selectionRect.x()
            val scaledWidth = baseImg().width() / scale
            selectionRect.width() = if (me.x < scaledWidth) newWidth else scaledWidth - selectionRect.x()

            val newHeight = me.y - selectionRect.y()
            val scaledHeight = baseImg().height() / scale
            selectionRect.height() = if (me.y < scaledHeight) newHeight else scaledHeight - selectionRect.y()
            drawWebcamPreview(graphicsContext2D)

          case _ => {}
        }
      }
    }

    def scale = baseImg().width() / width()

    def drawWebcamPreview(gc: GraphicsContext) = {
      gc.clearRect(0, 0, width.get, width.get)
      val old = gc.stroke

      val scale = baseImg().width() / width()
      gc.drawImage(baseImg(), 0, 0, baseImg().width() / scale, baseImg().height() / scale)

      gc.stroke = Color.Red
      gc.strokeRect(selectionRect.x() - 1, selectionRect.y() - 1, selectionRect.width() + 2, selectionRect.height() + 2)
      gc.stroke = old
    }

    drawWebcamPreview(graphicsContext2D)
  }
}
