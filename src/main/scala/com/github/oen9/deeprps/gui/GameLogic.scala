package com.github.oen9.deeprps.gui

import scalafx.scene.image.WritableImage
import java.awt.image.BufferedImage
import zio.Exit
import com.github.oen9.deeprps.RpsType._
import scala.util.Random
import scalafx.embed.swing.SwingFXUtils
import zio.Exit._
import zio.Cause

object GameLogic {
  def handlePreview(playerImg: WritableImage, evalImg: BufferedImage => Exit[Throwable, RpsType]): com.github.oen9.deeprps.RpsType.RpsType = {
    val bufferedImage = SwingFXUtils.fromFXImage(playerImg, null)
    evalImg(bufferedImage) match {
      case Failure(cause) => com.github.oen9.deeprps.RpsType.Paper
      case Success(playerChoice) => playerChoice
    }
  }

  def handlePlay(gameState: GameState, playerImg: WritableImage, evalImg: BufferedImage => Exit[Throwable, RpsType]): Option[Cause[Throwable]] = {
    gameState.botChoice() = outcomes(Random.nextInt(3))

    val bufferedImage = SwingFXUtils.fromFXImage(playerImg, null)
    evalImg(bufferedImage) match {
      case Failure(cause) => Some(cause)
      case Success(playerChoice) =>
        gameState.playerChoice() = playerChoice
        gameState.gameResult() match {
          case Bot => gameState.botScore() = gameState.botScore() + 1
          case Player => gameState.playerScore() = gameState.playerScore() + 1
          case Draw =>
        }
        None
    }
  }
}
