package Demo

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import Demo.Data._

class LoginTest extends Simulation {

  val httpConf = http.baseUrl(Data.url)
    .acceptHeader("application/json")
    .check(status.is(200))

  val scn = scenario("Login").
    exec(http("login")
      .post("/users/login")
      .body(StringBody(
        s"""{
          "email": "${Data.email}",
          "password": "${Data.password}"
        }"""
      )).asJson
      .check(status.is(200))
      .check(jsonPath("$.token").notNull)
    )
  setUp(scn.inject(atOnceUsers(20)))
}