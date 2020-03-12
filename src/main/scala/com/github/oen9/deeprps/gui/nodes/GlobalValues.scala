package com.github.oen9.deeprps.gui.nodes

import com.github.oen9.deeprps.gui.GameState
import zio.Exit
import com.github.oen9.deeprps.RpsType
import java.awt.image.BufferedImage

trait GlobalValues {
  val gameState: GameState = new GameState()
  def evaluateImage: BufferedImage => Exit[Throwable, RpsType.RpsType]
}
