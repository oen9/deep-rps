package com.github.oen9.deeprps.modules

import zio._
import zio.system.System

object UserHomeNotFound extends Exception(
  """|user.home env variable not found.
     |Report a bug with info how to get your home directory.""".stripMargin
)

object pathProvider {
  type PathProvider = Has[PathProvider.Service]

  object PathProvider {
    trait Service {
      def getCfgDir(): IO[Throwable, String]
      def getModelPath(): IO[Throwable, String]
    }

    val live: ZLayer[System, Nothing, PathProvider] = ZLayer.fromFunction { system => 
      new Service {
        val cfgDir = "/.deep-rps"
        val modelName = "/model.zip"

        def getCfgDir(): IO[Throwable,String] = for {
          maybeHomeDir <- system.get.property("user.home")
          homeDir <- ZIO.fromOption(maybeHomeDir).mapError(_ => UserHomeNotFound)
        } yield homeDir + cfgDir

        def getModelPath(): IO[Throwable,String] = for {
          cfgDir <- getCfgDir()
        } yield cfgDir + modelName
      }
    }

    val test: ZLayer.NoDeps[Nothing, PathProvider] = ZLayer.succeed(new Service {
      def getCfgDir(): IO[Throwable,String] = IO.succeed("/tmp")
      def getModelPath(): IO[Throwable,String] = for {
        cfgDir <- getCfgDir()
      } yield cfgDir + "/deep-rps/model.zip"
    })

  }

  def getCfgDir(): ZIO[PathProvider, Throwable, String] =
    ZIO.accessM[PathProvider](_.get.getCfgDir())
  def getModelPath(): ZIO[PathProvider, Throwable, String] =
    ZIO.accessM[PathProvider](_.get.getModelPath())
}
