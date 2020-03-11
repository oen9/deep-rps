ThisBuild / scalaVersion     := "2.13.1"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.github.oen9"
ThisBuild / organizationName := "oen9"

val zioVersion = "1.0.0-RC18"
val zioMacrosVersion = "0.6.2"
val dl4jVersion = "1.0.0-beta6"

lazy val javaFXModules = Seq("base", "controls", "fxml", "graphics", "media", "swing", "web")
lazy val osName = System.getProperty("os.name") match {
  case n if n.startsWith("Linux")   => "linux"
  case n if n.startsWith("Mac")     => "mac"
  case n if n.startsWith("Windows") => "win"
  case _ => throw new Exception("Unknown platform!")
}

lazy val root = (project in file("."))
  .settings(
    name := "deep-rps",
    libraryDependencies ++= Seq(
      "org.deeplearning4j" % "deeplearning4j-core" % dl4jVersion,
      "org.nd4j" % "nd4j-native-platform" % dl4jVersion,
      "org.nd4j" % "nd4j-native" % dl4jVersion classifier "linux-x86_64-avx2",
      "org.bytedeco" % "javacv-platform" % "1.5.2",

      "org.typelevel" %% "cats-core" % "2.1.1",
      "dev.zio" %% "zio-interop-cats" % "2.0.0.0-RC11",
      "dev.zio" %% "zio" % zioVersion,
      "dev.zio" %% "zio-logging-slf4j" % "0.2.3",
      //"dev.zio" %% "zio-macros-core" % zioMacrosVersion,
      //"dev.zio" %% "zio-macros-test" % zioMacrosVersion,

      "org.scalafx" %% "scalafx" % "12.0.2-R18",
      "com.github.scopt" %% "scopt" % "4.0.0-RC2",
      "ch.qos.logback" % "logback-classic" % "1.2.3",

      "dev.zio" %% "zio-test" % zioVersion % Test,
      "dev.zio" %% "zio-test-sbt" % zioVersion % Test,
    ),
    libraryDependencies ++= javaFXModules.map(m => "org.openjfx" % s"javafx-$m" % "12.0.2" classifier osName),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
    scalacOptions ++= Seq(
      "-Xlint",
      "-unchecked",
      "-deprecation",
      "-feature",
      "-language:higherKinds",
      "-Ymacro-annotations"
    ),
  )
  .enablePlugins(JavaAppPackaging)
