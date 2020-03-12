package com.github.oen9.deeprps.gui.nodes

import java.awt.image.BufferedImage
import zio.Exit
import com.github.oen9.deeprps.RpsType

object Nodes {
  def apply(
    evalImg: BufferedImage => Exit[Throwable, RpsType.RpsType]
  ) = new Images
        with GlobalValues
        with SizeValues
        with ImageViews
        with Webcam
        with Labels
        with Buttons
        with Timers {
      val evaluateImage = evalImg
    }
}
