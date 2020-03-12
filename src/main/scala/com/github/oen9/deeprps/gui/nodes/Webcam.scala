package com.github.oen9.deeprps.gui.nodes

import com.github.oen9.deeprps.gui.webcam.WebcamService
import com.github.oen9.deeprps.gui.webcam.WebcamPreview

trait Webcam {
  this: Images with SizeValues =>

  val webcamService = new WebcamService
  val canvas = WebcamPreview(imgWidth, imgHeight, selectionRect, webcamPreviewImg)
}
