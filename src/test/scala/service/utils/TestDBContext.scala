package service.utils

import com.sksamuel.elastic4s.testkit.{ElasticSugar, IndexMatchers, SearchMatchers}
import org.scalatest.WordSpecLike
import service.utils.db.DBContext

import scala.util.Random

/**
 * Project: rest-ads
 * Package: service.utils
 * Created by asoloviov on 6/29/17 5:36 PM.
 */
trait TestDBContext extends WordSpecLike with ElasticSugar with SearchMatchers with IndexMatchers {
  trait DBContextContext {
    val appConfig = AppConfig()
    val dbContext = new DBContext(() => client, Random.alphanumeric.take(5).mkString.toLowerCase + "_", appConfig)
  }
}
