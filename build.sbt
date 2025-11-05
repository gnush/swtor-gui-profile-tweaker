ThisBuild / version := "0.1.0"
ThisBuild / organization := "io.github.gnush"
ThisBuild / scalaVersion := "3.3.6"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-encoding", "utf8")

// sbt pack
enablePlugins(PackPlugin)
packMain := Map("guiStateTweaker" -> "io.github.gnush.profiletweaker.MainApp")
//packJvmOpts := Map("guiStateTweaker" -> Seq(
//  "--enable-native-access=javafx.graphics,ALL-UNNAMED",
//  "--sun-misc-unsafe-memory-access=allow"
//))

lazy val root = (project in file("."))
  .settings(
    name := "SW:ToR GUI State Tweaker"
  )

libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "2.4.0"
libraryDependencies += "com.lihaoyi" %% "os-lib" % "0.11.6"

// JavaFX Modules:
//     needed: base, media, graphics, controls
//     not needed: web, swing, fxml
libraryDependencies += "org.scalafx" %% "scalafx" % "23.0.1-R34" exclude ("org.openjfx", "javafx-web") exclude ("org.openjfx", "javafx-swing") exclude ("org.openjfx", "javafx-fxml")

// will exclude scalafx from the pack result
// libraryDependencies += "org.scalafx" %% "scalafx" % "23.0.1-R34" % "provided"

//val foo = System.setProperty("os.name", "Windows")  // works, but would need to run this three times with different values, also a bit hacky
//libraryDependencies ++= {
//  // Determine OS version of JavaFX binaries
//  lazy val osName = System.getProperty("os.name") match {
//    case n if n.startsWith("Linux") => "linux"
//    case n if n.startsWith("Mac") => "mac"
//    case n if n.startsWith("Windows") => "win"
//    case _ => throw new Exception("Unknown platform!")
//  }
//  Seq("base", "controls", "fxml", "graphics", "media", "swing", "web")
//    .map(m => "org.openjfx" % s"javafx-$m" % "23" classifier osName)
//}

// works, but includes all in the same build, making the packed result bigger than needed
//libraryDependencies ++= Seq("linux", "mac", "win") flatMap (osName =>
//  Seq("base", "controls", "fxml", "graphics", "media", "swing", "web")
//    .map(m => "org.openjfx" % s"javafx-$m" % "23" classifier osName))
libraryDependencies ++= Seq("linux", "mac", "win") flatMap (osName =>
  Seq("base", "controls", "graphics", "media")
    .map(m => "org.openjfx" % s"javafx-$m" % "23" classifier osName))

fork := true