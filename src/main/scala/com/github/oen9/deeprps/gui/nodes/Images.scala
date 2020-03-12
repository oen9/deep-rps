package com.github.oen9.deeprps.gui.nodes

import scalafx.scene.image.Image
import scalafx.beans.property.ObjectProperty

trait Images {
  def createImage(src: String) = {
    new Image(getClass().getResourceAsStream(src))
  }

  val rockImg = createImage("/img/rps/rock.png")
  val paperImg = createImage("/img/rps/paper.png")
  val scissorsImg = createImage("/img/rps/scissors.png")
  val webcamFakePreviewImg = createImage("/img/rps/webcam-fake-preview.jpg")
  val webcamPreviewImg = ObjectProperty[Image](webcamFakePreviewImg)
}
