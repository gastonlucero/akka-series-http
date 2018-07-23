package lowlevel

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink}
import scala.concurrent.Future
import scala.util.{Failure, Success}


/**
  * Examples on how to start a WebServer with lowlevel
  *
  * https://doc.akka.io/docs/akka-http/10.0.11/scala/http/server-side/low-level-api.html
  *
  * For test, Run this class and execute curl commands, or in the browser
  * curl -H "Accept: application/xml" -H "Content-Type: application/xml" -X GET http://localhost:9000/
  * curl -H "Accept: application/xml" -H "Content-Type: application/xml" -X GET http://localhost:9000/ping
  *
  * curl -H "Accept: application/xml" -H "Content-Type: application/xml" -X GET http://localhost:9100/
  * curl -H "Accept: application/xml" -H "Content-Type: application/xml" -X GET http://localhost:9100/ping (How we can handle this case??)
  *
  * curl -H "Accept: application/xml" -H "Content-Type: application/xml" -X GET http://localhost:9200/
  * curl -H "Accept: application/xml" -H "Content-Type: application/xml" -X GET http://localhost:9200/ping
  *
  */
object LowLevelHttpServer extends App {

  implicit val system = ActorSystem("lowLevel")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher


  // RequestHandler with function : HttpRequest => HttpResponse
  val functionWayRequestHandler: HttpRequest => HttpResponse = {
    case HttpRequest(GET, Uri.Path("/"), _, _, _) => {
      println("GET / => FunctionWay")
      HttpResponse(entity = HttpEntity(ContentTypes.`text/html(UTF-8)`, "<html><body><b>L&L! Function Way</b></body></html>"))
    }

    case HttpRequest(GET, Uri.Path("/ping"), _, _, _) => {
      println("GET /ping => FunctionWay")
      HttpResponse(entity = "Pong!")
    }

    case r: HttpRequest => {
      println("Unknown")
      r.discardEntityBytes() // Important Related to slide 25 = StreamingWay
      HttpResponse(404, entity = " FunctionWay Unknown resource!")
    }
  }
  val serverSource = Http().bind(interface = "localhost", port = 9000)
  val bindingFuture: Future[Http.ServerBinding] =
    serverSource.to(Sink.foreach {
      connection => connection.handleWithSyncHandler(functionWayRequestHandler) // connection = IncomingConnection
    }
    ).run()
  bindingFuture.onComplete{
    case Success(s) => println("LowLevel Server in FunctionWay at port 9000")
    case Failure(f) => println("Error LowLevel Server in FunctionWay at port 9000")
  }

  // This is the same
  //
  // val serverSync = Http().bindAndHandleSync(functionWayRequestHandler,interface = "localhost", port = 9000)

  Http().serverLayer()

  // RequestHandler with "Async" Function : HttpRequest => Future[HttpResponse]
  val requestHandlerAsync: HttpRequest => Future[HttpResponse] = {
    case HttpRequest(GET, Uri.Path("/"), _, _, _) => {
      println("GET / => AsyncWay")
      Future(HttpResponse(entity = HttpEntity(ContentTypes.`text/html(UTF-8)`, "<html><body><b>L&L! Future way</b></body></html>")))
    }
    case HttpRequest(GET, Uri.Path("/ping"), _, _, _) => {
      println("GET /ping => AsyncWay")
      Future(HttpResponse(entity = "Pong!"))
    }
    case r: HttpRequest => {
      println("Unknown")
      r.discardEntityBytes() // Important Related to slide 25 = StreamingWay
      Future(HttpResponse(404, entity = " AsyncWay Unknown resource!"))
    }
  }

  val serverAsync :Future[Http.ServerBinding] = Http().bindAndHandleAsync(requestHandlerAsync, interface = "localhost", port =
    9200)
  serverAsync.onComplete{
      case Success(s) => println("LowLevel Server in AsyncWay at port 9200")
      case Failure(f) => println("Error LowLevel Server in AsyncWay at port 9200")
    }

  // RequestHandler with Streams : Source[HttpRequest] => Flow[HttpRequest,HttpResponse] => Sink[HttpResponse]
  val flowHandler: Flow[HttpRequest, HttpResponse, _] = Flow[HttpRequest].map(httpRequest => {
    httpRequest match {
      case HttpRequest(GET, Uri.Path("/"), _, _, _) => {
        println("GET / => FlowWay")
        HttpResponse(entity = HttpEntity(ContentTypes.`text/html(UTF-8)`, "<html><body><b>L&L! Stream Way</b></body></html>"))
      }
      case HttpRequest(GET, Uri.Path("/ping"), _, _, _) => {
        println("GET /ping => FlowWay")
        HttpResponse(entity = "Pong!")
      }
      case HttpRequest(_, _, _, _, _) => {
        httpRequest.discardEntityBytes()
        println("Unknown endpoint")
        HttpResponse(status = StatusCodes.NotFound, entity = HttpEntity(ContentTypes.`text/html(UTF-8)`, "FlowWay " +
          "Unknown endpoint"))
      }
    }
  })
  val serverFlow : Future[Http.ServerBinding] = Http().bindAndHandle(flowHandler, interface = "localhost", port = 9300)
  serverFlow.onComplete{
      case Success(s) => println("LowLevel Server in FlowWay at port 9300")
      case Failure(f) => println("Error LowLevel Server in FlowWay at port 9300")
    }
}
