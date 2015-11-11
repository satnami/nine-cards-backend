import sbt._

object NineCardsBuild extends Build with Settings with Dependencies {

  lazy val root = project.in(file(".")) aggregate(api, user, app)

  lazy val user = project.in(file("modules/user")) settings projectSettings ++ commonDeps

  lazy val app = project.in(file("modules/app")) settings projectSettings ++ appDeps

  lazy val api = project.in(file("modules/api"))
    .aggregate(user, app)
    .dependsOn(user, app)
    .settings(apiSettings ++ apiDeps)
}