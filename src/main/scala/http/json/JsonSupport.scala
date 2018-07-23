package http.json

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

case class UserTest(id: String, name: String, age: Int)

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {


  implicit val userJsonFormat = jsonFormat3(UserTest)
  // el numero se corresponde con la cantidad de atributos de la clase y al ser implicito y estar extendido en otro trait,
  // ya esta dentro del contexto tanto para leer json y transformarlo a User, como al reves

}
