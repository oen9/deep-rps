package com.github.oen9.deeprps.gui.webcam

import javafx.concurrent.Service
import scalafx.scene.image.Image
import javafx.concurrent.Task
import org.bytedeco.javacv.OpenCVFrameGrabber
import org.bytedeco.javacv.Java2DFrameUtils
import scalafx.embed.swing.SwingFXUtils

class WebcamService extends Service[Image] {
  def createTask(): Task[Image] = new Task[Image] {
    val initImg = new Image(getClass().getResourceAsStream("/img/rps/webcam-fake-preview.jpg"))
    updateValue(initImg)

    def call(): Image = {
      val cam = new OpenCVFrameGrabber(0)
      //cam.setImageWidth(1920)
      //cam.setImageHeight(1080)
      //cam.setImageWidth(1280)
      //cam.setImageHeight(720)
      cam.setImageWidth(640)
      cam.setImageHeight(480)
      cam.start()
      try {
        while(!isCancelled()) {
          val grabbedImg = cam.grab()
          val bufferedImage = Java2DFrameUtils.toBufferedImage(grabbedImg)
          updateValue(SwingFXUtils.toFXImage(bufferedImage, null))
        }
        getValue()
      } finally {
        cam.stop()
        updateValue(initImg)
      }
    }
  }
}
