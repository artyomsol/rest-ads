package service.utils.db

import service.utils.AppConfig

/**
 * Project: rest-ads
 * Package: service.utils.db
 * Created by asoloviov on 6/28/17 6:39 PM.
 */
class DBContext(implicit val cassandraContext: CassandraContext) {
  def close(): Unit = {
    cassandraContext.close()
  }
}
object DBContext {
  def apply(config: AppConfig = AppConfig()): DBContext = forConfigPath(config)

  def forConfigPath(cnf: AppConfig = AppConfig(), path: String = "app.db.default") = {
    val cassandraContext = CassandraContext.cassandraSessionForConfigPath(cnf.config, path)
    new DBContext()(cassandraContext)
  }

  def forProfileName(cnf: AppConfig = AppConfig(), profileName: String) = forConfigPath(cnf, s"app.db.$profileName")
}