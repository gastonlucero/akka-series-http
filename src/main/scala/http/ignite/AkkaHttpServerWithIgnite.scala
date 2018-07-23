package http.ignite

import scala.io.StdIn
import akka.http.scaladsl.server.Directives._
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import http.customDirective.AdvancedDirectivesRoutes

object AkkaHttpServerWithIgnite extends App with  IgniteRoute with AdvancedDirectivesRoutes{

  implicit val system: ActorSystem = ActorSystem("StratioActorSystem")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher //Para usar Future

  val rutas = route ~ routes
  val http = Http().bindAndHandle(rutas, "0.0.0.0", 8090)

  println("Enter para terminar")
  StdIn.readLine()
  http.flatMap(_.unbind()).onComplete(_ => system.terminate())

}
