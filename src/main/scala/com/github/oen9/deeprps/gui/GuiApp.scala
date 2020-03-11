package com.github.oen9.deeprps.gui

import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import scalafx.scene.control.Label
import scalafx.scene.layout.BorderPane
import scalafx.scene.layout.VBox
import scalafx.scene.control.Button
import scalafx.scene.layout.HBox
import scalafx.geometry.Pos
import scalafx.scene.layout.Priority

import scalafx.scene.image.Image
import javafx.scene.image.{Image => JImage}
import scalafx.scene.image.ImageView
import scalafx.scene.paint.Color
import java.awt.image.BufferedImage
import com.github.oen9.deeprps.RpsType
import zio.Exit
import com.github.oen9.deeprps.RpsType.Rock
import com.github.oen9.deeprps.RpsType.Paper
import com.github.oen9.deeprps.RpsType.Scissors
import scalafx.scene.control.Alert
import scalafx.scene.control.Alert.AlertType
import scalafx.beans.binding.Bindings
import javafx.scene.paint.Paint
import com.github.oen9.deeprps.gui.geometry.Rect
import com.github.oen9.deeprps.gui.webcam.WebcamPreview
import scalafx.beans.property.ObjectProperty
import com.github.oen9.deeprps.gui.webcam.WebcamService
import scalafx.scene.image.WritableImage
import scalafx.scene.control.ToggleButton
import scalafx.geometry.Insets

object GuiApp {
  def run(evalImg: BufferedImage => Exit[Throwable, RpsType.RpsType]) = {
    val imgWidth = 200d
    val imgHeight = 151
    val gameState = GameState()
    val selectionRect = new Rect(10, 20, 60, 80)

    val rockImg = new Image(getClass().getResourceAsStream("/img/rps/rock.png"))
    val paperImg = new Image(getClass().getResourceAsStream("/img/rps/paper.png"))
    val scissorsImg = new Image(getClass().getResourceAsStream("/img/rps/scissors.png"))
    val webcamFakePreviewImg = new Image(getClass().getResourceAsStream("/img/rps/webcam-fake-preview.jpg"))
    val webcamPreviewImg = ObjectProperty[Image](webcamFakePreviewImg)

    val webcamService = new WebcamService

    val botImgView = new ImageView(rockImg) {
      preserveRatio = true
      fitHeight = imgHeight
      fitWidth = imgWidth
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

    val canvas = WebcamPreview(imgWidth, imgHeight, selectionRect, webcamPreviewImg)

    val jfxApp = new JFXApp {
      val evalResultLabel = new Label("paper") {
        text <== Bindings.createStringBinding(() => gameState.playerChoice().toString, gameState.playerChoice)
      }
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
        textFill <== Bindings.createObjectBinding[Paint](() => gameState.gameResult() match {
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

      val playButton = new Button("play!") {
        onAction = { _ =>
          val realImg  = webcamPreviewImg()
          val scale = realImg.getWidth() / imgWidth
          val playerImg = new WritableImage(realImg.pixelReader.get,
            (selectionRect.x() * scale).toInt,
            (selectionRect.y() * scale).toInt,
            (selectionRect.width() * scale).toInt,
            (selectionRect.height() * scale).toInt
          )
          playerImgView.image = playerImg

          GameLogic
            .handlePlay(gameState, playerImg, evalImg)
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

      override def stopApp(): Unit = webcamService.cancel()

      stage = new PrimaryStage {
        title = "rock paper scissors"
        height = 600
        width = 600
        scene = new Scene {
          stylesheets.add("dark.css")
          root = new VBox(4) {
            alignment = Pos.Center
            children = Seq(
              new BorderPane {
                padding = Insets(25)
                center = webcamDescription
                right = new VBox(2) {
                  alignment = Pos.Center
                  children = Seq(
                    canvas,
                    webcamButton
                  )
                }
              },
              new HBox(3) {
                alignment = Pos.Center
                vgrow = Priority.Always
                children = Seq(
                  new VBox(2) {
                    hgrow = Priority.Always
                    alignment = Pos.Center
                    children = Seq(
                      botScoreLabel,
                      botImgView
                    )
                  },
                  new VBox(1) {
                    hgrow = Priority.Always
                    alignment = Pos.Center
                    children = Seq(playButton)
                  },
                  new VBox(3) {
                    hgrow = Priority.Always
                    alignment = Pos.Center
                    children = Seq(
                      playerScoreLabel,
                      playerImgView,
                      evalResultLabel
                    )
                  },
                )
              },
              new HBox(1) {
                alignment = Pos.Center
                children = Seq(gameResultLabel)
              },
              new BorderPane {
                right = new Label("by oen")
              },
            )
          }
        }
      }
    }

    jfxApp.main(Array())
  }
}
