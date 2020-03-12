package com.github.oen9.deeprps.gui.nodes

import scalafx.scene.control.Label
import scalafx.beans.binding.Bindings
import com.github.oen9.deeprps.gui.Draw
import com.github.oen9.deeprps.gui.Bot
import com.github.oen9.deeprps.gui.Player
import scalafx.scene.paint.Color
import javafx.scene.paint.{Paint => JPaint}

trait Labels {
  this: Images with GlobalValues =>

  val evalResultLabel = new Label("unknown") {
    text <== Bindings.createStringBinding(() => gameState.playerChoice().toString, gameState.playerChoice)
  }

  val evalPreviewLabel = new Label("unknown")

  val webcamDescription = new Label("""|Webcam preview ->
                                       |Unfortunately there is no object detection yet.
                                       |Please select your hand on the preview.""".stripMargin)

  val gameResultLabel = new Label("no result yet") {
    style = "-fx-font-size: 40"
    text <== Bindings.createStringBinding(() => gameState.gameResult() match {
      case Draw => "DRAW"
      case Bot => "Bot wins"
      case Player => "Player wins"
    }, gameState.gameResult)
    textFill <== Bindings.createObjectBinding[JPaint](() => gameState.gameResult() match {
      case Draw => Color.LightBlue
      case Bot => Color.IndianRed
      case Player => Color.Chartreuse
    }, gameState.gameResult)
  }

  val botScoreLabel = new Label("0") {
    text <== Bindings.createStringBinding(() => gameState.botScore().toString, gameState.botScore)
  }

  val playerScoreLabel = new Label("0") {
    text <== Bindings.createStringBinding(() => gameState.playerScore().toString, gameState.playerScore)
  }
}
