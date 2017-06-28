package service.utils.db

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import com.sksamuel.elastic4s.ElasticClient
import com.typesafe.config.Config
import org.elasticsearch.common.settings.Settings
import service.model.db.AdvertsDAO
import service.utils.AppConfig

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{Await, ExecutionContext}
import scala.util.Try

/**
 * Project: rest-ads
 * Package: service.utils.db
 * Created by asoloviov on 6/28/17 6:39 PM.
 */
class DBContext(esClientCreator: () => ElasticClient,
                val indexPrefix: String = "",
                val appConfig: AppConfig) {
  def getESClient: ElasticClient = esClientCreator()

  val advertsDAO = AdvertsDAO()(this)

  def init(timeout: FiniteDuration)(implicit system: ActorSystem): this.type = {
    implicit val ec: ExecutionContext = system.dispatcher
    implicit def log: LoggingAdapter = system.log
    val initialized = Await.result(advertsDAO.checkOrCreateIndex(), timeout)
    if (initialized) this else throw new ExceptionInInitializerError("DBContext initialization failed")
  }
}

object DBContext {
  def apply(config: AppConfig = AppConfig()): DBContext = forConfigPath(config)

  def forConfigPath(cnf: AppConfig = AppConfig(), path: String = "app.db.default") = {
    val prefix = esIndexPrefixForConfigPath(cnf.config, path)
    val setPrefixTo = if (prefix.length > 0) prefix + "_" else ""
    new DBContext(getESClientForConfigPath(cnf.config, path), setPrefixTo, cnf)
  }

  def forProfileName(cnf: AppConfig = AppConfig(), profileName: String) = forConfigPath(cnf, s"app.db.$profileName")

  private def getESClientForConfigPath(cnf: Config, path: String = "app.db.default"): () => ElasticClient = () => {
    val cfg = cnf.getConfig(s"$path.es")
    val settings = Settings.settingsBuilder()
      .put("cluster.name", cfg.getString("cluster.name"))
      .put("node.name", cfg.getString("cluster.name"))
      .put("es.logger.level", Try(cfg.getString("logger.level")).getOrElse("INFO"))
      .build()
    ElasticClient.transport(settings, "elasticsearch://" + cfg.getString("cluster.connect"))
  }

  private def esIndexPrefixForConfigPath(cnf: Config, path: String = "app.db.default"): String = cnf.getString(s"$path.es.index.prefix")

}