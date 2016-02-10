import sbt.Keys._
import sbt._

trait Dependencies {
  this: Build =>

  val sprayHttp = "io.spray" %% "spray-can" % Versions.spray
  val sprayRouting = "io.spray" %% "spray-routing-shapeless2" % Versions.spray
  val sprayTestKit = "io.spray" %% "spray-testkit" % Versions.spray % "test" exclude("org.specs2", "specs2_2.11")
  val specs2Core = "org.specs2" %% "specs2-core" % Versions.specs2 % "test"


  val circeCore = "io.circe" %% "circe-core" % Versions.circe
  val circeGeneric = "io.circe" %% "circe-generic" % Versions.circe
  val circeParser = "io.circe" %% "circe-parser" % Versions.circe

  val cats = "org.typelevel" %% "cats" % "0.4.0" // TODO move this to version

  val akkaActor = "com.typesafe.akka" %% "akka-actor" % Versions.akka

  val googleplayCrawler = "com.akdeniz" % "googleplaycrawler" % Versions.googleplayCrawler

  val baseDepts = Seq(specs2Core)

  val apiDeps = Seq(libraryDependencies ++= baseDepts ++ Seq(
    cats,
    sprayHttp,
    sprayRouting,
    sprayTestKit,
    akkaActor,
    googleplayCrawler,
    circeCore,
    circeGeneric,
    circeParser))
}
