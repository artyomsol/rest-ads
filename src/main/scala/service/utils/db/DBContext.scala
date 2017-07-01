package service.utils.db

import akka.actor.ActorSystem
import akka.event.slf4j.Logger
import com.sksamuel.elastic4s.TcpClient
import com.typesafe.config.Config
import org.elasticsearch.common.settings.Settings
import service.model.db.AdvertsDAO
import service.utils.{AppConfig, DBInitializationException}

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration
import scala.util.Try

/**
 * Project: rest-ads
 * Package: service.utils.db
 * Created by asoloviov on 6/28/17 6:39 PM.
 */
class DBContext(esClientCreator: () => TcpClient,
                val indexPrefix: String = "",
                val appConfig: AppConfig) {
  def getESClient: TcpClient = esClientCreator()

  val advertsDAO = AdvertsDAO()(this)

  def init(timeout: FiniteDuration = appConfig.dbInitializationTimeout)(implicit system: ActorSystem): Future[DBContext] = {
    import system.dispatcher
    val timeOutFuture = akka.pattern.after(timeout, system.scheduler)(Future.failed(DBInitializationException("Timed out")))
    val createIndexFuture = advertsDAO.checkOrCreateIndex().map(initialized =>
      if (initialized) this else throw DBInitializationException()
    )
    Future.firstCompletedOf(Seq(timeOutFuture, createIndexFuture))
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

  private def getESClientForConfigPath(cnf: Config, path: String = "app.db.default"): () => TcpClient = () => {
    val cfg = cnf.getConfig(s"$path.es")
    val settings = Settings.builder()
      .put("cluster.name", cfg.getString("cluster.name"))
      .put("node.name", cfg.getString("cluster.name"))
      .put("logger.level", Try(cfg.getString("logger.level")).getOrElse("INFO"))
      .build()
    TcpClient.transport(settings, "elasticsearch://" + cfg.getString("cluster.connect"))
  }

  private def esIndexPrefixForConfigPath(cnf: Config, path: String = "app.db.default"): String = cnf.getString(s"$path.es.index.prefix")

}