package http.ignite

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive0, Route}
import http.json.{JsonSupport, UserTest}

trait IgniteRoute extends JsonSupport {

  val cache = IgniteHelper.igniteDataGrid("cache")
  cache.put("1", UserTest("1", "cacheUser", 10))

  val cacheValidation: PartialFunction[String, UserTest] = {
    new PartialFunction[String, UserTest] {
      override def isDefinedAt(key: String) = cache.containsKey(key)

      override def apply(v1: String) = cache.get(v1)
    }
  }

  def cacheDirective(key: String): Directive0 = {
    cacheValidation.isDefinedAt(key) match {
      case true => {
        println("From cache")
        complete(cacheValidation(key))
      }
      case _ => mapInnerRoute {
        route =>
          ctx => {
            route(ctx)
          }
      }
    }
  }

  val routes: Route = pathPrefix("ignite") {
    get {
      path(Segment) { id =>
        cacheDirective(id) {
          println("From service")
          val user = UserTest(id, s"$id test", 20)
          cache.put(id, user)
          complete(user)
        }
      }
    }
  }
}
