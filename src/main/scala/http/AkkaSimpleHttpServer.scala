package http

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpRequest, HttpResponse}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow

import scala.io.StdIn

object AkkaSimpleHttpServer extends App {

  implicit val system: ActorSystem = ActorSystem("StratioActorSystem")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher //Para usar Future

  val flowHandler = Flow.fromFunction[HttpRequest, HttpResponse](httpRequest => {
    HttpResponse(entity = HttpEntity(ContentTypes.`text/plain(UTF-8)`, "pong"))
  })

  val http = Http().bindAndHandle(flowHandler, "0.0.0.0", 8090)

  println("Enter para terminar")
  StdIn.readLine()
  http.flatMap(_.unbind()).onComplete(_ => system.terminate())

}
