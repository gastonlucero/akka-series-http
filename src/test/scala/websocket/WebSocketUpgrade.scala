package websocket

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ContentTypes
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.http.scaladsl.server.Directives.{handleWebSocketMessages, pathPrefix, _}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}

import scala.util.Random

/**
  * Run this file , with /static endpoint
  */
object WebSocketUpgrade extends App {

  implicit val system = ActorSystem("ws")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val random = Random
  val randomIterable = new scala.collection.immutable.Iterable[Int] {
    override def iterator = new Iterator[Int] {
      override def hasNext = true

      override def next() = {
        val ret = random.nextInt()
        ret
      }
    }
  }

  def randomFlow: Flow[Message, Message, Any] =
    Flow[Message].flatMapConcat {
      case tm: TextMessage =>
        tm.textStream.map(s => s.toInt).flatMapConcat { qty =>
          Source(randomIterable).take(qty).map(_.toString)
        } map { s =>
          TextMessage(Source.single(s))
        }
      case bm: BinaryMessage =>
        // ignore binary messages but drain content to avoid the stream being clogged
        bm.dataStream.runWith(Sink.ignore)
        Source.empty
    }

  val websocketRoute = pathPrefix("websocket") {
    handleWebSocketMessages(randomFlow)
  } ~ path("static") {
    getFromResource("web/index.html", ContentTypes.`text/html(UTF-8)`)
  }

  val bindingFuture = Http().bindAndHandle(websocketRoute, "localhost", 7000)

}
