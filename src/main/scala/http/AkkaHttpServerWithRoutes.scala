package http


import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer

import scala.io.StdIn

object AkkaHttpServerWithRoutes extends App {

  implicit val system: ActorSystem = ActorSystem("StratioActorSystem")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher //Para usar Future

  lazy val route: Route =
    pathSingleSlash {
      complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<html><body><b>L6L!</b></body></html>"))
    } ~
      path("ping") {
        get {
          complete(StatusCodes.OK, "Pong!")
        }
      } ~
      path("ping" / Segment) {
        pathParam =>
          get {
            complete(StatusCodes.OK, s"Pong $pathParam!")
          }
      } ~
      path("pingUrlParam") {
        parameter('number.as[Int]) {
          numberParameter => {
            complete(200 -> HttpEntity(ContentTypes.`application/json`, s"Pong $numberParameter!"))
          }
        }
      } ~
      path("pingHeader") {
        (parameter('id.as[String]) | headerValueByName("myHeader")) {
          (valor) =>
          complete(s"Pong with header = $valor")
        }
      }
  val http = Http().bindAndHandle(route, "0.0.0.0", 8090)

  println("Enter para terminar")
  StdIn.readLine()
  http.flatMap(_.unbind()).onComplete(_ => system.terminate())

}
