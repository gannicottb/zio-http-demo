ThisBuild / organization := "com.example"
ThisBuild / scalaVersion := "2.13.5"

lazy val root = (project in file(".")).settings(
  name := "zio-http-demo",
  libraryDependencies ++= Seq(
//    "dev.zio" %% "zio" % "1.0.13",
//    "io.d11" %% "zhttp"      % "1.0.0.0-RC25"
    "dev.zio" %% "zio" % "2.0.0-RC2",
    "io.d11" %% "zhttp" % "2.0.0-RC4"
  )
)