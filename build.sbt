ThisBuild / version := "0.1.0"
ThisBuild / name := "SW:ToR GUI Profile Tweaker"
ThisBuild / organization := "io.github.gnush"
ThisBuild / scalaVersion := "3.3.6"
ThisBuild / assemblyMergeStrategy := {
  case PathList("META-INF", _*) => MergeStrategy.discard
  case PathList("module-info.class") => MergeStrategy.discard
  case _ => MergeStrategy.deduplicate
}

Compile / mainClass := Some("io.github.gnush.profiletweaker.MainApp")

lazy val root = (project in file("."))
  .settings(
    name := "SwtorGuiProfileTweaker"
  )

libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "2.4.0"
libraryDependencies += "org.scalafx" %% "scalafx" % "23.0.1-R34"
libraryDependencies += "com.lihaoyi" %% "os-lib" % "0.11.6"

libraryDependencies ++= {
  // Determine OS version of JavaFX binaries
  lazy val osName = System.getProperty("os.name") match {
    case n if n.startsWith("Linux") => "linux"
    case n if n.startsWith("Mac") => "mac"
    case n if n.startsWith("Windows") => "win"
    case _ => throw new Exception("Unknown platform!")
  }
  Seq("base", "controls", "fxml", "graphics", "media", "swing", "web")
    .map(m => "org.openjfx" % s"javafx-$m" % "23" classifier osName)
}