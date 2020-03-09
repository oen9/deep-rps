package com.github.oen9.deeprps

object RpsType {
  sealed trait RpsType
  case object Rock extends RpsType
  case object Paper extends RpsType
  case object Scissors extends RpsType
}
