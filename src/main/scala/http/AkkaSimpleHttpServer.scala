package http

import com.sun.deploy.net.{HttpRequest, HttpResponse}
import com.sun.tools.javac.comp.Flow

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
