package com.github.oen9.deeprps.gui.nodes

import javafx.scene.image.{Image => JImage}
import scalafx.scene.image.ImageView
import com.github.oen9.deeprps.RpsType._
import scalafx.beans.binding.Bindings

trait ImageViews {
  this: Images with SizeValues with GlobalValues =>

  val botImgView = new ImageView(rockImg) {
    preserveRatio = true
    fitHeight = imgWidth
    fitWidth = imgHeight
    println("gamestate: " + gameState)
    println("rockImg: " + rockImg)
    image <== Bindings.createObjectBinding[JImage](() => gameState.botChoice() match {
      case Rock => rockImg
      case Paper => paperImg
      case Scissors => scissorsImg
    }, gameState.botChoice)
  }

  val playerImgView = new ImageView(webcamFakePreviewImg) {
    preserveRatio = true
    fitHeight = imgHeight
    fitWidth = imgWidth
  }
}
