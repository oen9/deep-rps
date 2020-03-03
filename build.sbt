ThisBuild / scalaVersion     := "2.13.1"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.github.oen9"
ThisBuild / organizationName := "oen9"

val zioVersion = "1.0.0-RC17"
val zioMacrosVersion = "0.6.2"
val dl4jVersion = "1.0.0-beta6"

lazy val root = (project in file("."))
  .settings(
    name := "deep-rps",
    libraryDependencies ++= Seq(
      "org.deeplearning4j" % "deeplearning4j-core" % dl4jVersion,
      "org.nd4j" % "nd4j-native-platform" % dl4jVersion,
      "org.nd4j" % "nd4j-native" % dl4jVersion classifier "linux-x86_64-avx2",
      "dev.zio" %% "zio" % zioVersion,
      "dev.zio" %% "zio-macros-core" % zioMacrosVersion,
      "dev.zio" %% "zio-macros-test" % zioMacrosVersion,
      "com.github.scopt" %% "scopt" % "4.0.0-RC2",

      "org.slf4j" % "slf4j-simple" % "1.7.25",
      "org.slf4j" % "slf4j-api" % "1.7.25",

      "dev.zio" %% "zio-test" % zioVersion % Test,
      "dev.zio" %% "zio-test-sbt" % zioVersion % Test,
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
  )
  .enablePlugins(JavaAppPackaging)
