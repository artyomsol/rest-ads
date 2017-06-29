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
      ADEntity(Some("e029819db7b34917a3a277625f3e660e"), "Audi A4 Avant", gasoline, 100, `new` = true) shouldBe a[ADEntity]
    }
    "accept option fields" in {
      ADEntity(Some("e029819db7b34917a3a277625f3e660e"), "Audi A4 Avant", diesel, 100, `new` = false, mileage = Some(100000), Some(DateTime.now().withTimeAtStartOfDay())) shouldBe a[ADEntity]
    }
    "require option fields for used car ads" in {
      an[IllegalArgumentException] should be thrownBy ADEntity(Some("e029819db7b34917a3a277625f3e660e"), "Audi A4 Avant", diesel, 100, `new` = false, mileage = None, Some(DateTime.now().withTimeAtStartOfDay()))
    }
    "require title non empty AD title" in {
      an[IllegalArgumentException] should be thrownBy ADEntity(Some("e029819db7b34917a3a277625f3e660e"), "", gasoline, 10, `new` = true)
    }
    "require strictly positive values for price" in {
      an[IllegalArgumentException] should be thrownBy ADEntity(Some("e029819db7b34917a3a277625f3e660e"), "Audi A4 Avant", gasoline, 0, `new` = true)
      an[IllegalArgumentException] should be thrownBy ADEntity(Some("e029819db7b34917a3a277625f3e660e"), "Audi A4 Avant", gasoline, -10, `new` = true)
    }
    "require non negative values mileage" in {
      ADEntity(Some("e029819db7b34917a3a277625f3e660e"), "Audi A4 Avant", gasoline, 10, `new` = false, mileage = Some(0), Some(DateTime.now().withTimeAtStartOfDay())) shouldBe a[ADEntity]
      an[IllegalArgumentException] should be thrownBy ADEntity(Some("e029819db7b34917a3a277625f3e660e"), "Audi A4 Avant", gasoline, 10, `new` = false, mileage = Some(-10), Some(DateTime.now().withTimeAtStartOfDay()))
    }
    "require past tense dates in firest registration field" in {
      an[IllegalArgumentException] should be thrownBy ADEntity(Some("e029819db7b34917a3a277625f3e660e"), "Audi A4 Avant", gasoline, 10, `new` =false, mileage = Some(10), Some(DateTime.now().plusDays(1)))
    }
  }
  it should {
    import spray.json._
    "deserialized from JSON" in {
      val jsonOldCarAD = """{"mileage":100000,"price":100,"fuel":"diesel","id":"e029819db7b34917a3a277625f3e660e","new":false,"first registration":"2017-06-27T00:00:00.000Z","title":"Audi A4 Avant"}"""
      val jsonNewCarAD = """{"mileage":null,"price":100,"fuel":"diesel","id":"e029819db7b34917a3a277625f3e660e","new":true,"title":"Audi A4 Avant"}"""
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
  it should {
    "convert itself to ADEntityUpdate instance" in {
      val updateAD = oldCarAD.toUpdateAD
      updateAD shouldEqual fullUpdateCarAD
    }
  }
}
