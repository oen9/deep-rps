package com.github.oen9.deeprps.utils

import zio._
import java.nio.file.Files
import java.nio.file.Paths

object fileUtil {
  type FileUtil = Has[FileUtil.Service]

  object FileUtil {
    trait Service {
     def createDirectories(dirPath: String): Task[Unit]
    }

    val live: ZLayer.NoDeps[Throwable, FileUtil] = ZLayer.succeed(new Service {
      def createDirectories(dirPath: String): Task[Unit] = ZIO.effect {
        val path = Paths.get(dirPath)
        Files.createDirectories(path)
      }
    })

    val test: ZLayer.NoDeps[Throwable, FileUtil] = ZLayer.succeed(new Service {
      def createDirectories(dirPath: String): Task[Unit] = ZIO.unit
    })
  }

  def createDirectories(dirPath: String): ZIO[FileUtil, Throwable, Unit] =
    ZIO.accessM[FileUtil](_.get.createDirectories(dirPath))
}
