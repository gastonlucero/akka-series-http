package http.json

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer

import scala.io.StdIn

object AkkaHttpServerWithJson extends App with  JsonDirectivesRoutes {

  implicit val system: ActorSystem = ActorSystem("StratioActorSystem")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher //Para usar Future

  val http = Http().bindAndHandle(route, "0.0.0.0", 8080)

  println("Enter para terminar")
  StdIn.readLine()
  http.flatMap(_.unbind()).onComplete(_ => system.terminate())

}
