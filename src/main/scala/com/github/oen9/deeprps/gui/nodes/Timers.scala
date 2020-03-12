package com.github.oen9.deeprps.gui.nodes

import java.{util => ju}
import scalafx.application.Platform
import com.github.oen9.deeprps.gui.GameLogic
import com.github.oen9.deeprps.gui.utils.ImgUtil

trait Timers {
  this: Images
    with SizeValues
    with GlobalValues
    with Labels =>

  val evalPreviewTimer = new ju.Timer
  val evalPreviewTask = new ju.TimerTask {
    def run(): Unit = Platform.runLater {
      val playerImg = ImgUtil.scalePlayerImg(webcamPreviewImg(), imgWidth, selectionRect)
      evalPreviewLabel.text = GameLogic.handlePreview(playerImg, evaluateImage).toString()
    }
  }
}
