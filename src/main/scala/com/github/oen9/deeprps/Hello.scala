package com.github.oen9.deeprps

import zio._
import zio.logging._
import com.github.oen9.deeprps.modules.pathProvider
import com.github.oen9.deeprps.utils.fileUtil

object Hello extends App {
  def run(args: List[String]): ZIO[ZEnv,Nothing,Int] =
    app(args)
      .flatMapError(logThrowable)
      .ensuring(logInfo("App finished"))
      .provideCustomLayer(
        slf4j.Slf4jLogger.make((_, msg) => msg) ++
        pathProvider.PathProvider.live ++
        fileUtil.FileUtil.live)
      .fold(_ => 1, _ => 0)

  def app(args: List[String]) = for {
    _ <- logInfo("App started")
    appArgs <- ZIO.fromEither(AppArgs.parse(args))
    _ <- if (appArgs.quit) ZIO.unit
         else AppArgsHandler.handle(appArgs, unsafeRunSync[Throwable, RpsType.RpsType](_))
  } yield ()
}
