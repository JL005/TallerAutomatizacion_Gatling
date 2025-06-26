package Demo

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import Demo.Data._
import scala.concurrent.duration._
import Demo.TokenStore._

class AddContactTest extends Simulation {
  // Configuración base
  val httpProtocol = http
    .baseUrl(url) // Cambia esto por tu URL
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")

  val scn = scenario("Login una vez y luego llamadas autenticadas")
    .exec(
      http("Login")
        .post("/users/login")
        .body(StringBody(
          s"""{
                "email": "${Data.email}",
                "password": "${Data.password}"
              }"""
        )).asJson
        .check(status.is(200))
        .check(jsonPath("$.token").saveAs("authToken"))
    ).exec(session => {
      session("authToken").asOption[String] match {
        case Some(token) =>
          println(s"✅ Token obtenido: $token")
          session
        case None =>
          println("❌ No se obtuvo el token")
          session.markAsFailed
      }
    })
    .exec(
      http("Llamada autenticada")
        .post("/contacts")
        .body(StringBody(
          s"""{
                "firstName": "Juan",
                "lastName": "Lopera",
                "birthdate": "1977-07-07",
                "email": "jf@fake.com",
                "phone": "8005555555",
                "street1": "1 Main St.",
                "street2": "Apartment A",
                "city": "Medellín",
                "stateProvince": "Ant",
                "postalCode": "12345",
                "country": "CO"
          }"""
        )).asJson
        .header("Authorization", "Bearer ${authToken}")
        .check(status.is(201))
    )


  setUp(
    scn.inject(atOnceUsers(10)) // Puedes cambiar el número de usuarios
  ).protocols(httpProtocol)
}
