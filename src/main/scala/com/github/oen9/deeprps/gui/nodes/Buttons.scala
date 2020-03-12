package com.github.oen9.deeprps.gui.nodes

import scalafx.scene.control.Button
import scalafx.scene.control.ToggleButton
import com.github.oen9.deeprps.gui.GameLogic
import com.github.oen9.deeprps.gui.utils.ImgUtil
import scalafx.scene.control.Alert
import scalafx.scene.control.Alert.AlertType

trait Buttons {
  this: Images
    with SizeValues
    with ImageViews
    with GlobalValues
    with Webcam =>

  val playButton = new Button("play!") {
    onAction = { _ =>
      val playerImg = ImgUtil.scalePlayerImg(webcamPreviewImg(), imgWidth, selectionRect)
      playerImgView.image = playerImg

      GameLogic
        .handlePlay(gameState, playerImg, evaluateImage)
        .foreach { cause =>
          new Alert(AlertType.Error) {
            title = "NeuralNetwork error"
            headerText = "Something went wrong!"
            contentText = "Short error (more in logs):\n" + cause.prettyPrint.substring(0, 1000) + "..."
          }.showAndWait()
        }
    }
  }

  val webcamButton = new ToggleButton("switch to webcam") {
    selected.onChange((_, _, newVal) =>
      if (newVal) {
        webcamService.restart()
        webcamPreviewImg <== webcamService.valueProperty
        text = "switch to imgage"
      }
      else {
        webcamService.cancel()
        webcamPreviewImg.unbind()
        webcamPreviewImg() = webcamFakePreviewImg
        text = "switch to webcam"
      }
    )
  }
}
