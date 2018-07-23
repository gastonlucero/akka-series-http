package http.customDirective

import scala.io.StdIn

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer

object AkkaHttpServerWithCustomDirective extends App with AdvancedDirectivesRoutes {

  implicit val system: ActorSystem = ActorSystem("StratioActorSystem")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher //Para usar Future

  val http = Http().bindAndHandle(route, "0.0.0.0", 8090)

  println("Enter para terminar")
  StdIn.readLine()
  http.flatMap(_.unbind()).onComplete(_ => system.terminate())

}
