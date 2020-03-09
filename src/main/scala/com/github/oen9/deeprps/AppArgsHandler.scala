package com.github.oen9.deeprps

import zio._
import com.github.oen9.deeprps.modules.pathProvider
import java.io.File
import com.github.oen9.deeprps.utils.fileUtil
import com.github.oen9.deeprps.gui.GuiApp
import java.awt.image.BufferedImage
import com.github.oen9.deeprps.RpsType.RpsType

object AppArgsHandler {
  def handle(args: AppArgs, runZIO: ZIO[Unit, Throwable, RpsType] => Exit[Throwable, RpsType]) = for {
    _ <- args.trainDir.map(trainModel).getOrElse(ZIO.unit)
    _ <- evalImg(args.eval)
    unsafeEvalImgHandler <- createUnsafeEvalImgHandler(runZIO)
    _ <- if (args.gui) ZIO.effect(GuiApp.run(unsafeEvalImgHandler)) else ZIO.unit
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

  private def createUnsafeEvalImgHandler(runZIO: ZIO[Unit, Throwable, RpsType] => Exit[Throwable, RpsType]) = {
    def createHandler(loadPath: String, logging: zio.logging.Logging.Service) = {
      bImg: BufferedImage => runZIO(
        DeepModel
          .evalBufferedImg(loadPath, bImg)
          .provideLayer(ZLayer.succeed(logging))
      )
    }

    for {
      loadPath <- pathProvider.getModelPath()
      logging <- ZIO.accessM[zio.logging.Logging](l => ZIO.succeed(l.get))
      handler =  createHandler(loadPath, logging)
    } yield handler
  }
}
