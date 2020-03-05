package com.github.oen9.deeprps

import cats.implicits._
import zio.interop.catz.core._
import zio._
import zio.console._
import com.github.oen9.deeprps.modules.pathProvider
import java.io.File
import com.github.oen9.deeprps.utils.fileUtil

object AppArgsHandler {
  def handle(args: AppArgs) = for {
    _ <- putStrLn("hello")
    _ <- args.trainDir.map(trainModel).getOrElse(ZIO.unit)
    _ <- evalImg(args.eval)
    savedModelPath <- pathProvider.getCfgDir()
    _ <- putStrLn(savedModelPath)
  } yield ()

  def trainModel(trainData: File) = for {
    cfgDir <- pathProvider.getCfgDir()
    _ <- fileUtil.createDirectories(cfgDir)
    savePath <- pathProvider.getModelPath()
    _ <- ZIO.effect(DeepModel.trainAndSave(savePath, trainData.toString()))
  } yield  ()

  def evalImg(imgs: Vector[File]) = for {
    loadPath <- pathProvider.getModelPath()
    _ <- imgs.traverse { img =>
      ZIO.effect(DeepModel.loadAndEval(loadPath, img))
    }
  } yield ()
}
