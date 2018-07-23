package routes

import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.get
import akka.http.scaladsl.server.directives.PathDirectives.path
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{Matchers, WordSpec}

/**
  * *
  * Basic structure of tests:
  * REQUEST ~> ROUTE ~> check {
  * ASSERTIONS
  * }
  *
  * El test prueba las rutas del paquete json
  */
class TestKitRoutesSpec extends WordSpec with Matchers with ScalaFutures with ScalatestRouteTest{

  //Aca definimos la ruta que queremos probar , o simplemente podemos hacer esta clase use un trait en donde definimos la
  //ruta, por ejemplo agregado with JsonDirectivesRoutes

  val routes: Route =
    pathSingleSlash {
      complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<html><body><b>L&L!</b></body></html>"))
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

  "Simple Routes" should {

    "handle (GET /)" in {
      Get("/") ~> routes ~> check {
        status should ===(StatusCodes.OK)
        contentType should ===(ContentTypes.`text/html(UTF-8)`)
        responseAs[String] shouldEqual "<html><body><b>L&L!</b></body></html>"
      }
    }

    "return pong response for (GET /ping)" in {
      val request = HttpRequest(method = HttpMethods.GET, uri = "/ping")
      request ~> routes ~> check {
        status should ===(StatusCodes.OK)
        responseAs[String] shouldEqual "Pong!"
      }
    }

    "when request with pathParam return pong $number response for (GET /ping/$number)" in {
      Get("/ping/10") ~> routes ~> check {
        status should ===(StatusCodes.OK)
        responseAs[String] shouldEqual "Pong 10!"
      }
    }

    "when urlParam return pong $number response for (GET /ping?$number)" in {
      val number = 20
      val request = HttpRequest(uri = s"/pingUrlParam?number=$number")
      request ~> routes ~> check {
        status should ===(StatusCodes.OK)
        contentType should ===(ContentTypes.`application/json`)
        responseAs[String] shouldEqual s"Pong $number!"
      }
    }

    "when context doesnt exists it is not handled" in {
      Get("/invalid") ~> routes ~> check {
        handled shouldBe false
      }
    }

    "when you send a header " in {
      Get("/pingHeader") ~> RawHeader("myHeader", "stratio") ~> routes ~> check {
        status shouldEqual StatusCodes.OK
        responseAs[String] shouldEqual "Pong with header = stratio"
      }
    }


  }
}
