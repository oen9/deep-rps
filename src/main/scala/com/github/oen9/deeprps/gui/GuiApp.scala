package com.github.oen9.deeprps.gui

import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.scene.Scene
import scalafx.scene.control.Label
import scalafx.scene.layout.BorderPane
import scalafx.scene.layout.VBox
import scalafx.scene.layout.HBox
import scalafx.geometry.Pos
import scalafx.scene.layout.Priority

import java.awt.image.BufferedImage
import com.github.oen9.deeprps.RpsType
import zio.Exit
import scalafx.geometry.Insets
import com.github.oen9.deeprps.gui.nodes.Nodes

object GuiApp {
  def run(evalImg: BufferedImage => Exit[Throwable, RpsType.RpsType]) = {

    val jfxApp = new JFXApp {
      val nodes = Nodes(evalImg)
      nodes.evalPreviewTimer.scheduleAtFixedRate(nodes.evalPreviewTask, 1000, 250)

      override def stopApp(): Unit = nodes.webcamService.cancel()

      stage = new PrimaryStage {
        icons.add(nodes.rockImg)
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
                center = nodes.webcamDescription
                right = new VBox(3) {
                  alignment = Pos.Center
                  children = Seq(
                    nodes.canvas,
                    nodes.evalPreviewLabel,
                    nodes.webcamButton
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
                      nodes.botScoreLabel,
                      nodes.botImgView
                    )
                  },
                  new VBox(1) {
                    hgrow = Priority.Always
                    alignment = Pos.Center
                    children = Seq(nodes.playButton)
                  },
                  new VBox(3) {
                    hgrow = Priority.Always
                    alignment = Pos.Center
                    children = Seq(
                      nodes.playerScoreLabel,
                      nodes.playerImgView,
                      nodes.evalResultLabel
                    )
                  },
                )
              },
              new HBox(1) {
                alignment = Pos.Center
                children = Seq(nodes.gameResultLabel)
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
