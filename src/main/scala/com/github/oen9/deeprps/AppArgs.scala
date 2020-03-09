package com.github.oen9.deeprps

import cats.implicits._
import java.io.File

case class AppArgs(
  quit: Boolean = false,
  eval: Vector[File] = Vector(),
  trainDir: Option[File] = None,
  gui: Boolean = false,
)

class WrongAppArgsException(msg: String) extends Exception(msg)

object AppArgs {

  def parse(args: List[String]): Either[WrongAppArgsException, AppArgs] = {
    import scopt.{ OParserSetup, DefaultOParserSetup }
    val setup: OParserSetup = new DefaultOParserSetup {
      override def showUsageOnError: Option[Boolean] = Some(true)
      override def terminate(exitState: Either[String,Unit]): Unit = () // fix for sys.exit inside ZIO
    }

    import scopt.OParser
    val builder = OParser.builder[AppArgs]
    val parser1 = {
      import builder._
      OParser.sequence(
        programName("deep-rps"),
        head("deep-rps", "0.0.1"),
        opt[Unit]('x', "x-gui")
          .action((_, c) => c.copy(gui = true))
          .text("run GUI"),
        opt[File]('e', "eval")
          .unbounded()
          .optional()
          .valueName("<file>")
          .action((x, c) => c.copy(eval = c.eval :+ x))
          .text("image to eval (can be used few times to provide multiple files)"),
        opt[File]('t', "train")
          .valueName("<dir>")
          .action((x, c) => c.copy(trainDir = x.some))
          .text("dir with train and test subdirs"),
        help("help")
          .text("prints this usage text"),
        version("version")
          .text("prints app version")
      )
    }

    OParser
      .parse(parser1, args, AppArgs(), setup)
      .map(aa => { // fix for sys.exit inside ZIO
        if (args.exists(a => a == "--help" || a == "--version"))
          aa.copy(quit = true)
        else
          aa
      })
      .toRight(new WrongAppArgsException("To use app first fix args"))
  }
}
