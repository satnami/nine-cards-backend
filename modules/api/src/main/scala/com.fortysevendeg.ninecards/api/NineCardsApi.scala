package com.fortysevendeg.ninecards.api

import akka.actor.Actor
import com.fortysevendeg.ninecards.api.FreeUtils._
import com.fortysevendeg.ninecards.api.NineCardsApiHeaderCommons._
import com.fortysevendeg.ninecards.api.messages.UserMessages._
import com.fortysevendeg.ninecards.api.converters.Converters._
import com.fortysevendeg.ninecards.api.messages.InstallationsMessages._
import com.fortysevendeg.ninecards.processes.NineCardsServices.NineCardsServices
import com.fortysevendeg.ninecards.processes.domain._
import com.fortysevendeg.ninecards.processes.{AppProcesses, UserProcesses}
import spray.httpx.SprayJsonSupport
import spray.routing._

import scala.language.{higherKinds, implicitConversions}
import scalaz.concurrent.Task

class NineCardsApiActor
  extends Actor
    with NineCardsApi
    with AuthHeadersRejectionHandler
    with NineCardsExceptionHandler {

  def actorRefFactory = context

  def receive = runRoute(nineCardsApiRoute)

}

trait NineCardsApi
  extends HttpService
    with SprayJsonSupport
    with JsonFormats {

  def nineCardsApiRoute(implicit appProcesses: AppProcesses[NineCardsServices], userProcesses: UserProcesses[NineCardsServices]): Route =
    userApiRoute() ~
      installationsApiRoute() ~
      appsApiRoute() ~
      swaggerApiRoute

  private[this] def userApiRoute()(implicit userProcesses: UserProcesses[NineCardsServices]) =
    pathPrefix("login") {
      pathEndOrSingleSlash {
        requestLoginHeaders {
          (appId, apiKey) =>
            post {
              entity(as[ApiLoginRequest]) {
                request =>
                  complete {
                    val result: Task[ApiLoginResponse] = userProcesses.signUpUser(request) map toApiLoginResponse
                    result
                  }
              }
            }
        }
      }
    }

  private[this] def installationsApiRoute()(implicit userProcesses: UserProcesses[NineCardsServices]) =
    pathPrefix("installations") {
      pathEndOrSingleSlash {
        requestFullHeaders {
          (appId, apiKey, sessionToken, androidId, marketLocalization) =>
            put {
              entity(as[ApiUpdateInstallationRequest]) {
                request =>
                  /* TODO: The userId should be fetched after authorizing the user through the sessionToken - Issue 266 */
                  implicit val userId = 1l
                  implicit val deviceAndroidId = androidId

                  complete {
                    val result: Task[ApiUpdateInstallationResponse] =
                      userProcesses.updateInstallation(request) map toApiUpdateInstallationResponse
                    result
                  }
              }
            }
        }
      }
    }

  private[this] def appsApiRoute()(implicit appProcesses: AppProcesses[NineCardsServices]) =
    pathPrefix("apps") {
      path("categorize") {
        get {
          complete {
            val result: Task[Seq[GooglePlayApp]] = appProcesses.categorizeApps(Seq("com.fortysevendeg.ninecards"))
            result
          }
        }
      }
    }

  private[this] def swaggerApiRoute =
  // This path prefix grants access to the Swagger documentation.
  // Both /apiDocs/ and /apiDocs/index.html are valid paths to load Swagger-UI.
    pathPrefix("apiDocs") {
      pathEndOrSingleSlash {
        getFromResource("apiDocs/index.html")
      } ~ {
        getFromResourceDirectory("apiDocs")
      }
    }
}