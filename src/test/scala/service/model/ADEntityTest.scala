package service.model

import org.joda.time.DateTime
import org.scalatest.{Matchers, WordSpec}
import service.model.FuelType._
import service.utils.TestData

/**
 * Project: rest-ads
 * Package: service.model
 * Created by asoloviov on 6/27/17 3:22 PM.
 */
class ADEntityTest extends WordSpec with Matchers with TestData {
  "Car adverts" should {
    "accept required fields" in {
      ADEntity(10L, "Audi A4 Avant", gasoline, 100, `new` = true) shouldBe a[ADEntity]
    }
    "accept option fields" in {
      ADEntity(10L, "Audi A4 Avant", diesel, 100, `new` = false, mileage = Some(100000), Some(DateTime.now().withTimeAtStartOfDay()))
    }
    "require option fields for used car ads" in {
      an[IllegalArgumentException] should be thrownBy ADEntity(10L, "Audi A4 Avant", diesel, 100, `new` = false, mileage = None, Some(DateTime.now().withTimeAtStartOfDay()))
    }
    "require title non empty AD title" in {
      an[IllegalArgumentException] should be thrownBy ADEntity(10L, "", gasoline, 10, `new` = true)
    }
  }
  it should {
    import spray.json._
    "deserialized from JSON" in {
      val jsonOldCarAD = """{"mileage":100000,"price":100,"fuel":"diesel","id":10,"new":false,"firstRegistration":"2017-06-27T00:00:00.000Z","title":"Audi A4 Avant"}"""
      val jsonNewCarAD = """{"mileage":null,"price":100,"fuel":"diesel","id":10,"new":true,"title":"Audi A4 Avant"}"""
      jsonOldCarAD.parseJson.convertTo[ADEntity] shouldEqual oldCarAD
      jsonNewCarAD.parseJson.convertTo[ADEntity] shouldEqual newCarAD
    }
    "serialized to JSON" in {
      val restoredNewCarAD = newCarAD.toJson.convertTo[ADEntity]
      restoredNewCarAD shouldBe a[ADEntity]
      restoredNewCarAD shouldEqual newCarAD
      val restoredOldCarAD = oldCarAD.toJson.convertTo[ADEntity]
      restoredOldCarAD shouldBe a[ADEntity]
      restoredOldCarAD shouldEqual oldCarAD
    }
  }
}