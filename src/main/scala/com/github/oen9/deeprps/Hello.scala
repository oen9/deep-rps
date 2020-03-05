package com.github.oen9.deeprps

import zio._
import java.io.StringWriter
import java.io.PrintWriter
import com.github.oen9.deeprps.modules.pathProvider

object Hello extends App {
  type AppEnv = zio.console.Console with pathProvider.PathProvider

  def run(args: List[String]): ZIO[ZEnv,Nothing,Int] =
    app(args)
      .flatMapError {
        case e: Throwable =>
          val sw = new StringWriter
          e.printStackTrace(new PrintWriter(sw))
          zio.console.putStrLn(sw.toString())
      }
      .provideCustomLayer(pathProvider.PathProvider.live)
      .fold(_ => 1, _ => 0)

  def app(args: List[String]): ZIO[AppEnv, Throwable, Unit] = for {
    appArgs <- ZIO.fromEither(AppArgs.parse(args))
    _ <- if (appArgs.quit) ZIO.unit
         else AppArgsHandler.handle(appArgs)
  } yield ()
}
