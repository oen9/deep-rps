package com.github.oen9.deeprps.gui.nodes

import scalafx.Includes._
import scalafx.scene.canvas.Canvas
import scalafx.scene.input.MouseEvent
import com.github.oen9.deeprps.gui.geometry.Rect
import scalafx.scene.canvas.GraphicsContext
import scalafx.scene.paint.Color
import scalafx.scene.image.Image

object WebcamPreview {
  def apply(width: Double, height: Double, selectionRect: Rect, baseImg: Image): Canvas = {
    new Canvas(width = width, height = height) {
      handleEvent(MouseEvent.Any) {
        me: MouseEvent => {
          me.eventType match {
            case MouseEvent.MousePressed =>
              selectionRect.x() = me.x
              selectionRect.y() = me.y
              selectionRect.width() = 0
              selectionRect.height() = 0
            case MouseEvent.MouseDragged =>
              selectionRect.width() = me.x - selectionRect.x()
              selectionRect.height() = me.y - selectionRect.y()
              drawWebcamPreview(graphicsContext2D)
            case _ => {}
          }
        }
      }
      def drawWebcamPreview(gc: GraphicsContext) = {
        gc.clearRect(0, 0, width.get, width.get)
        val old = gc.stroke
        gc.drawImage(baseImg, 0, 0)
        gc.stroke = Color.Red
        gc.strokeRect(selectionRect.x(), selectionRect.y(), selectionRect.width(), selectionRect.height())
        gc.stroke = old
      }

      drawWebcamPreview(graphicsContext2D)
    }
  }
}
