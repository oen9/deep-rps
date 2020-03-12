package com.github.oen9.deeprps.gui.utils

import scalafx.scene.image.Image
import scalafx.scene.image.WritableImage
import com.github.oen9.deeprps.gui.geometry.Rect

object ImgUtil {
  def scalePlayerImg(realImg: Image, imgWidthDst: Double, selectionRect: Rect) = {
    val scale = realImg.getWidth() / imgWidthDst
    new WritableImage(realImg.pixelReader.get,
      (selectionRect.x() * scale).toInt,
      (selectionRect.y() * scale).toInt,
      (selectionRect.width() * scale).toInt,
      (selectionRect.height() * scale).toInt
    )
  }
}
