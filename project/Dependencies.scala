/*
 * Copyright 2017 47 Degrees, LLC. <http://www.47deg.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
object Dependencies {

  import sbt._
  import sbt.Keys.libraryDependencies

  private def akka(suff: String) = "com.typesafe.akka" %% s"akka$suff" % Versions.akka
  private def circe(suff: String) = "io.circe" %% s"circe$suff" % Versions.circe
  private def doobie(suff: String) = "org.tpolecat" %% s"doobie$suff" % Versions.doobie exclude("org.scalaz", "*")
  private def enumeratum(suff: String) = "com.beachape" %% s"enumeratum$suff" % Versions.enumeratum
  private def http4s(suff: String) = "org.http4s" %% s"http4s$suff" % Versions.http4s
  private def scalaz(suff: String) = "org.scalaz" %% s"scalaz$suff" % Versions.scalaz
  private def specs2(suff: String) = "org.specs2" %% s"specs2$suff" % Versions.specs2 % "test"
  private def spray(suff: String) = "io.spray" %% s"spray$suff" % Versions.spray

  private val akkaActor = akka("-actor")
  private val akkaTestKit = akka("-testkit") % "test"
  private val cats = "org.typelevel" %% "cats" % Versions.cats
  private val embeddedRedis = "com.orange.redis-embedded" % "embedded-redis" % Versions.embeddedRedis % "test"
  private val flywaydbCore = "org.flywaydb" % "flyway-core" % Versions.flywaydb
  private val googleApiClient = "com.google.api-client" % "google-api-client" % Versions.googleApiClient exclude("com.google.guava", "*")
  private val hasher = "com.roundeights" %% "hasher" % Versions.hasher
  private val http4sClient = "org.http4s" %% "http4s-blaze-client" % Versions.http4s
  private val jodaConvert = "org.joda" % "joda-convert" % Versions.jodaConvert
  private val jodaTime = "joda-time" % "joda-time" % Versions.jodaTime
  private val mockserver = "org.mock-server" % "mockserver-netty" % Versions.mockserver % "test"
  private val newRelic = "com.newrelic.agent.java" % "newrelic-agent" % Versions.newRelic
  private val scalacheckDateTime  ="com.fortysevendeg" %% "scalacheck-datetime" % Versions.scalacheckDateTime % "test"
  private val scalacheckShapeless = "com.github.alexarchambault" %% "scalacheck-shapeless_1.13" % Versions.scalacheckShapeless % "test"
  private val scredis = "com.livestream" %% "scredis" % Versions.scredis
  private val specs2Core = specs2("-core") exclude("org.scalaz", "*")
  private val sprayJson = "io.spray" %% "spray-json" % Versions.sprayJson
  private val sprayTestKit = spray("-testkit") % "test" exclude("org.specs2", "specs2_2.11")
  private val tagSoup = "org.ccil.cowan.tagsoup" % "tagsoup" % Versions.tagSoup
  private val typesafeConfig = "com.typesafe" % "config" % Versions.typesafeConfig

  val baseDeps = Seq(
    hasher,
    scalacheckShapeless,
    scalaz("-concurrent"),
    scalaz("-core"),
    specs2("-cats"),
    specs2Core,
    specs2("-mock"),
    specs2("-scalacheck")
  )

  val apiDeps = Seq(libraryDependencies ++= baseDeps ++ Seq(
    akkaActor,
    akkaTestKit,
    cats % "test",
    circe("-core"),
    circe("-spray"),
    newRelic,
    spray("-can"),
    spray("-routing-shapeless2"),
    sprayJson,
    sprayTestKit
  ))

  val commonsDeps = Seq(libraryDependencies ++= Seq(
    cats,
    circe("-core"),
    circe("-generic"),
    circe("-parser"),
    embeddedRedis,
    enumeratum(""),
    jodaConvert,
    jodaTime,
    scredis,
    scalaz("-concurrent"),
    specs2Core,
    specs2("-scalacheck"),
    scalacheckDateTime,
    scalacheckShapeless,
    typesafeConfig
  ))

  val processesDeps = Seq(libraryDependencies ++= baseDeps)

  val servicesDeps = Seq(libraryDependencies ++= baseDeps ++ Seq(
    cats,
    circe("-core"),
    circe("-generic"),
    doobie("-contrib-hikari"),
    doobie("-contrib-postgresql"),
    doobie("-contrib-h2"),
    doobie("-contrib-specs2") % "test",
    doobie("-core"),
    enumeratum(""),
    enumeratum("-circe"),
    googleApiClient,
    flywaydbCore % "test",
    http4s("-blaze-client"),
    http4s("-circe"),
    jodaConvert,
    jodaTime,
    mockserver
  ))

  val googleplayDeps = Seq(sbt.Keys.libraryDependencies ++= Seq(
    cats,
    circe("-core"),
    circe("-generic"),
    circe("-parser"),
    embeddedRedis,
    enumeratum(""),
    enumeratum("-circe"),
    http4sClient,
    jodaConvert,
    jodaTime,
    mockserver,
    newRelic,
    scredis,
    scalacheckShapeless,
    specs2Core,
    specs2("-matcher-extra"),
    specs2("-mock"),
    specs2("-scalacheck"),
    tagSoup
  ))
}
