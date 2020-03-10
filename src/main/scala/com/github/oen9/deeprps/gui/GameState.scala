package com.github.oen9.deeprps.gui

import com.github.oen9.deeprps.RpsType._
import scalafx.beans.property.ObjectProperty
import scalafx.beans.property.IntegerProperty
import scalafx.beans.binding.Bindings

sealed trait GameResult
case object Player extends GameResult
case object Bot extends GameResult
case object Draw extends GameResult

case class GameState(botChoice: ObjectProperty[RpsType] = ObjectProperty(Paper),
                     playerChoice: ObjectProperty[RpsType] = ObjectProperty(Paper),
                     botScore: IntegerProperty = IntegerProperty(0),
                     playerScore: IntegerProperty = IntegerProperty(0)) {

  val gameResult = Bindings.createObjectBinding[GameResult](() => {
     if (botChoice() == playerChoice()) Draw
     else (botChoice(), playerChoice()) match {
       case (Scissors, Paper) => Bot
       case (Paper, Rock) => Bot
       case (Rock, Scissors) => Bot
       case _ => Player
     }
  }, botChoice, playerChoice)
}
