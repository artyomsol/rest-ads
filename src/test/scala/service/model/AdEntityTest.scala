package service.model

import org.joda.time.DateTime
import org.scalatest.{Matchers, WordSpec}
import service.model.FuelType._

/**
 * Project: rest-ads
 * Package: service.model
 * Created by asoloviov on 6/27/17 3:22 PM.
 */
class AdEntityTest extends WordSpec with Matchers {
  "Car adverts" should {
    "accept required fields" in {
      AdEntity(10L, "Audi A4 Avant", gasoline, 100, `new` = true) shouldBe a[AdEntity]
    }
    "accept option fields" in {
      AdEntity(10L, "Audi A4 Avant", diesel, 100, `new` = false, mileage = Some(100000), Some(DateTime.now().withTimeAtStartOfDay()))
    }
    "require option fields for used car ads" in {
      an[IllegalArgumentException] should be thrownBy AdEntity(10L, "Audi A4 Avant", diesel, 100, `new` = false, mileage = None, Some(DateTime.now().withTimeAtStartOfDay()))
    }
    "require title non empty AD title" in {
      an[IllegalArgumentException] should be thrownBy AdEntity(10L, "", gasoline, 10, `new` = true)
    }
  }
  it should {
    import spray.json._
    val oldCarAD = AdEntity(10L, "Audi A4 Avant", diesel, 100, `new` = false, mileage = Some(100000), Some(DateTime.parse("2017-06-27T00:00:00.000Z")))
    val newCarAD = AdEntity(10L, "Audi A4 Avant", diesel, 100, `new` = true)
    "deserialized from JSON" in {
      val jsonOldCarAD = """{"mileage":100000,"price":100,"fuel":"diesel","id":10,"new":false,"firstRegistration":"2017-06-27T00:00:00.000Z","title":"Audi A4 Avant"}"""
      val jsonNewCarAD = """{"mileage":null,"price":100,"fuel":"diesel","id":10,"new":true,"title":"Audi A4 Avant"}"""
      jsonOldCarAD.parseJson.convertTo[AdEntity] shouldEqual oldCarAD
      jsonNewCarAD.parseJson.convertTo[AdEntity] shouldEqual newCarAD
    }
    "serialized to JSON" in {
      val restoredNewCarAD = newCarAD.toJson.convertTo[AdEntity]
      restoredNewCarAD shouldBe a[AdEntity]
      restoredNewCarAD shouldEqual newCarAD
      val restoredOldCarAD = oldCarAD.toJson.convertTo[AdEntity]
      restoredOldCarAD shouldBe a[AdEntity]
      restoredOldCarAD shouldEqual oldCarAD
    }
  }
}
