package service.model

import org.scalatest.{Matchers, WordSpec}
import service.utils.TestData

/**
 * Project: rest-ads
 * Package: service.model
 * Created by asoloviov on 6/29/17 6:00 PM.
 */
class ADEntityUpdateTest extends WordSpec with Matchers with TestData {

  "ADEntityUpdateTest" should {

    "require uptede to title field to be non empty string" in {
      assertThrows[IllegalArgumentException]( new ADEntityUpdate(title = Some("")))
    }

    "update advert with applyTo method" in {
      updateToOldCarAD.applyTo(newCarAD) shouldEqual oldCarAD
    }

    "convert itself to ADEntity with toADEntityWithID" in {
      fullUpdateCarAD.toADEntityWithID(oldCarAD.id.get).get shouldEqual oldCarAD
    }

  }
}
