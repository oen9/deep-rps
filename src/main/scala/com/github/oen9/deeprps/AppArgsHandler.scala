package com.github.oen9.deeprps

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
  } yield ()

  def trainModel(trainData: File) = for {
    cfgDir <- pathProvider.getCfgDir()
    _ <- fileUtil.createDirectories(cfgDir)
    savePath <- pathProvider.getModelPath()
    _ <- DeepModel.trainAndSave(savePath, trainData.toString())
  } yield  ()

  def evalImg(imgs: Vector[File]) = for {
    loadPath <- pathProvider.getModelPath()
    _ <- DeepModel.evalFiles(loadPath, imgs)
  } yield ()
}
