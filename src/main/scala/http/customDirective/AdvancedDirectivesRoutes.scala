package http.customDirective

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.MethodDirectives.get
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.http.scaladsl.server.{Directive1, Route}

//curl --header 'token: myToken' -v 'http://localhost:8080/token'
trait AdvancedDirectivesRoutes {

  val tokenFromHeader: Directive1[Option[String]] = optionalHeaderValueByName("token")
  val tokenFromParam: Directive1[Option[String]] = parameterMap.map(map => map.get("token"))

  val hasTokenDirective: Directive1[String] = (tokenFromHeader & tokenFromParam) tflatMap {
    case (Some(token), _) => provide(token)
    case (_, Some(token)) => provide(token)
    case _ => reject
  }

  lazy val route: Route =
    pathPrefix("token") {
      pathEnd {
        hasTokenDirective { token =>
          get {
            complete(StatusCodes.OK, s"Token equals $token")
          }
        }
      }
    }
}
