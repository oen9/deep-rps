package com.github.oen9.deeprps.gui

import scalafx.Includes._
import scalafx.application.JFXApp
import scalafx.application.JFXApp.PrimaryStage
import scalafx.geometry.Insets
import scalafx.scene.Scene
import scalafx.scene.control.Label
import scalafx.scene.layout.BorderPane
import scalafx.scene.layout.VBox
import scalafx.scene.control.Button
import scalafx.scene.layout.HBox
import scalafx.geometry.Pos
import scalafx.scene.layout.Priority

import scalafx.scene.image.Image
import scalafx.scene.image.ImageView
import scalafx.scene.canvas.Canvas
import scalafx.scene.canvas.GraphicsContext
import scalafx.scene.input.MouseEvent
import scalafx.scene.paint.Color
import scalafx.geometry.Rectangle2D
import scala.util.Random
import scalafx.scene.SnapshotParameters
import scalafx.embed.swing.SwingFXUtils
import java.awt.image.BufferedImage
import com.github.oen9.deeprps.RpsType.RpsType
import zio.Exit
import zio.Exit.Failure
import zio.Exit.Success
import scalafx.beans.property.DoubleProperty
import scalafx.scene.Node

// TODO refactor
object GuiApp {
  def run(evalImg: BufferedImage => Exit[Throwable, RpsType]) = {
    class Rect(
      _x: Double,
      _y: Double,
      _width: Double,
      _height: Double
    ) {
      val x = DoubleProperty(_x)
      val y = DoubleProperty(_y)
      val width = DoubleProperty(_width)
      val height = DoubleProperty(_height)

      def to2D() = new Rectangle2D(x(), y(), width(), height())
      def to2DWithShift(node: Node) = new Rectangle2D(
        x() + node.layoutX(),
        y() + node.layoutY(),
        width(),
        height()
      )
    }

    val imgWidth = 200d
    val imgHeight = 200d

    val rockImg = new Image(getClass().getResourceAsStream("/img/rps/rock.png"))
    val paperImg = new Image(getClass().getResourceAsStream("/img/rps/paper.png"))
    val scissorsImg = new Image(getClass().getResourceAsStream("/img/rps/scissors.png"))
    val botImgView = new ImageView(rockImg) {
      preserveRatio = true
      fitHeight = imgHeight
      fitWidth = imgWidth
    }

    val webcamFakePreviewImg = new Image(
      getClass().getResourceAsStream("/img/rps/webcam-fake-preview.jpg"),
      requestedWidth = imgWidth,
      requestedHeight = imgHeight,
      preserveRatio = true,
      smooth = true
    )

    val selectionRect = new Rect(10, 20, 60, 80)

    val imgView = new ImageView(webcamFakePreviewImg) {
      preserveRatio = true
      fitHeight = imgHeight
      fitWidth = imgWidth
      viewport = selectionRect.to2D()
    }

    val canvas = new Canvas(width = imgWidth, height = imgHeight)
    canvas.handleEvent(MouseEvent.Any) {
      me: MouseEvent => {
        me.eventType match {
          case MouseEvent.MousePressed =>
            selectionRect.x() = me.x
            selectionRect.y() = me.y
            selectionRect.width() = 0
            selectionRect.height() = 0
          case MouseEvent.MouseDragged =>
            selectionRect.width() = me.x - selectionRect.x()
            selectionRect.height() = me.y - selectionRect.y()
            drawBox(canvas.graphicsContext2D)
          case _ => {}
        }
      }
    }

    def drawBox(gc: GraphicsContext) = {
      gc.clearRect(0, 0, canvas.width.get, canvas.width.get)
      val old = gc.stroke
      gc.drawImage(webcamFakePreviewImg, 0, 0)
      gc.stroke = Color.Red
      gc.strokeRect(selectionRect.x(), selectionRect.y(), selectionRect.width(), selectionRect.height())
      gc.stroke = old
    }

    drawBox(canvas.graphicsContext2D)

    val jfxApp = new JFXApp {
      val evalResultLabel = new Label("here will be your result")
      val webcamDescription = new Label("""|Webcam preview ->
                                           |Unfortunately there is no object detection yet.
                                           |Please select your hand on the preview.""".stripMargin)

      val playButton = new Button("play!") {
        onAction = { e =>
          imgView.viewport = selectionRect.to2D()
          botImgView.image = Seq(rockImg, paperImg, scissorsImg)(Random.nextInt(3))

          val snapProp = new SnapshotParameters {
            viewport = selectionRect.to2DWithShift(canvas)
          }
          val writableImage = canvas.snapshot(snapProp, null)
          val bufferedImage = SwingFXUtils.fromFXImage(writableImage, null)
          // TODO remove | DEBUG code
          //import javax.imageio.ImageIO
          //import java.io.File
          //ImageIO.write(res, "png", new File("./test.png"));
          evalImg(bufferedImage) match {
            case Failure(cause) =>
              evalResultLabel.text = "error: " + cause.prettyPrint
              evalResultLabel.textFill = Color.Red
            case Success(value) =>
              evalResultLabel.text = value.toString
              evalResultLabel.textFill = Color.LightGreen
          }
        }
      }

      stage = new PrimaryStage { stag =>
        title = "rock paper scissors"
        height = 600
        width = 600
        scene = new Scene {
          stylesheets.add("dark.css")
          root = new VBox(3) {
            alignment = Pos.Center
            children = Seq(
              new BorderPane {
                padding = Insets(25)
                center = webcamDescription
                right = canvas
              },
              new HBox(3) {
                alignment = Pos.Center
                vgrow = Priority.Always
                children = Seq(
                  new VBox(1) {
                    hgrow = Priority.Always
                    alignment = Pos.Center
                    children = Seq(botImgView)
                  },
                  new VBox(1) {
                    hgrow = Priority.Always
                    alignment = Pos.Center
                    children = Seq(playButton)
                  },
                  new VBox(2) {
                    hgrow = Priority.Always
                    alignment = Pos.Center
                    children = Seq(
                      imgView,
                      evalResultLabel
                    )
                  },
                )
              },
              new BorderPane {
                padding = Insets(25)
                left = new Label("result:")
                center = new Label("YOU WON!")
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
