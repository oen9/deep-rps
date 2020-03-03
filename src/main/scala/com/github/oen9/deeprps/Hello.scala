package com.github.oen9

import zio._
import zio.console._
import java.io.StringWriter
import java.io.PrintWriter
import tpondertv.AppArgs

object Hello extends App {
  type AppEnv = zio.console.Console

  def run(args: List[String]): ZIO[ZEnv,Nothing,Int] =
    app(args)
      .flatMapError {
        case e: Throwable =>
          val sw = new StringWriter
          e.printStackTrace(new PrintWriter(sw))
          zio.console.putStrLn(sw.toString())
      }
      .fold(_ => 1, _ => 0)

  def app(args: List[String]): ZIO[AppEnv, Throwable, Unit] = for {
    appArgs <- ZIO.fromEither(AppArgs.parse(args))
    _ <- if (appArgs.quit) ZIO.unit
         else handleAppArgs(appArgs)
  } yield ()

  def handleAppArgs(args: AppArgs) = for {
    _ <- putStrLn("hello")
  } yield ()
}
