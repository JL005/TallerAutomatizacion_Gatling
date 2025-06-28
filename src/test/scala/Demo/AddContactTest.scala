package Demo

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import Demo.Data._
import scala.concurrent.duration._
import Demo.TokenStore._

class AddContactTest extends Simulation {
  val httpProtocol = http
    .baseUrl(Data.url)
    .acceptHeader("application/json")
    .contentTypeHeader("application/json")

  val scn = scenario("Add conctact")
    .exec(
      http("Add contacts")
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
        .header("Authorization", s"Bearer ${Data.authToken}")
        .check(status.is(201))
    )

  setUp(
    scn.inject(atOnceUsers(20))
  ).protocols(httpProtocol)
}
