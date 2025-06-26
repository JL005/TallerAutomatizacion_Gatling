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
    .check(status.is(200))

  // 🧩 Paso 1: Ejecutar login solo una vez por usuario virtual (VU)
  val loginOnce =
    exec(session => {
      if (!session.contains("loginDone")) {
        session.set("runLogin", true)
      } else {
        session.set("runLogin", false)
      }
    })
      .doIf(session => session("runLogin").as[Boolean]) {
        exec(
          http("Login")
            .post("/contacts")
            .body(StringBody(
              s"""{
                "email": "${Data.email}",
                "password": "${Data.password}"
              }"""
            )).asJson
            .check(jsonPath("$.token").saveAs("authToken"))
        )
          .exec(session => {
            session.set("loginDone", true)
          })
      }

  val scn = scenario("Login una vez y luego llamadas autenticadas")
    .exec(loginOnce)
    .repeat(1) {
      exec(
        http("Llamada autenticada")
          .post("/users/login")
          .body(StringBody(
            """{
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
          .header("Authorization", s"Bearer ${authToken}")
          .check(status.is(201))
      )
    }

  setUp(
    scn.inject(atOnceUsers(10)) // Puedes cambiar el número de usuarios
  ).protocols(httpProtocol)
}
