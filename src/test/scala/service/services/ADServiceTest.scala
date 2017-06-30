package service.services

import org.scalatest.Matchers
import service.model.ADEntity
import service.utils._

/**
 * Project: rest-ads
 * Package: service.services
 * Created by asoloviov on 6/29/17 4:55 PM.
 */
class ADServiceTest extends ServiceSpec with Matchers {

  "ADServiceTest" should {

    "getADByID" in new Context {
      val id = testAdverts(5).id.get
      adService.getADByID(id).await shouldEqual testAdverts(5)
    }

    "fail to getADByID of non existsing id" in new Context {
      val id = ADEntity.getNextID
      an[DataNotFoundException] should be thrownBy adService.getADByID(id).await
    }

    "generates id for new entity in createAD" in new Context {
      val testAD = randomAdvert.copy(id = None)
      val id = adService.createAD(testAD).await.id.get
      adService.getADByID(id).await shouldEqual testAD.withID(id)
    }

    "store AD with id defined with createAD" in new Context {
      adService.createAD(newCarAD).await shouldEqual newCarAD
      dbContext.advertsDAO.indexName should haveCount(testAdvertsNumber + 1)
      adService.getADByID(newCarAD.id.get).await shouldEqual newCarAD
    }

    "updateAD" in new Context {
      val id = testAdverts(3).id.get
      adService.updateAD(id, fullUpdateCarAD).flatMap(_ => adService.getADByID(id)).await shouldEqual oldCarAD.withID(id)
    }

    "getAllADs with default orderingby id" in new Context {
      val source = adService.getAllADs().await
      val result = source.runFold(List.empty[ADEntity]) { case (acc, e) => e :: acc }.await
      // collecting stream to the list puts the first element to the end of list, so reverse list then
      result.reverse should contain theSameElementsInOrderAs testAdverts.sortBy(_.id.get)
    }

    "getAllADs" in new Context {
      val source = adService.getAllADs("title", desc = true).await
      val result = source.runFold(List.empty[ADEntity]) { case (acc, e) => e :: acc }.await
      // do not restore the ordering because of desc = true
      result should contain theSameElementsInOrderAs testAdverts.sortBy(_.title)
    }

    "deleteAD the existing AD" in new Context {
      val id = testAdverts(7).id.get
      adService.deleteAD(id).await shouldEqual true
    }
    "fail to delete a non existing AD" in new Context {
      val id = ADEntity.getNextID
      an[DataNotFoundException] should be thrownBy adService.deleteAD(id).await
    }
  }
}
