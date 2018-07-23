package http.json

import akka.http.scaladsl.model.{HttpMethod, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.http.scaladsl.server.{Directive1, Route}

import scala.concurrent.{ExecutionContext, Future}

trait JsonDirectivesRoutes extends JsonSupport {

  implicit val executionContext: ExecutionContext

  lazy val composedRoutes = pathPrefix("composed") {
    route ~ {
      //In any other case
      complete(StatusCodes.MethodNotAllowed, "Only post or put are allowed")
    }
  }

  final val postOrPutDirective: Directive1[HttpMethod] = (post | put) & extractMethod

  lazy val route: Route = pathPrefix("users") {
    path("upsert") {
      postOrPutDirective { method => {
        entity(as[UserTest]) {
          user => {
            println(s"Method invoked ${method.value}")
            onSuccess(Future {
              UserTest("stratio", "startio", 2)
            }) {
              userUpsert => {
                complete(201, userUpsert)
              }
            }
          }
        }
      }
      }
    }
  }
}
