package service.utils

import com.typesafe.config.{Config, ConfigFactory}

import scala.util.Try

/**
 * Project: rest-ads
 * Package: service.utils
 * Created by asoloviov on 6/28/17 6:42 PM.
 */
class AppConfig(val config: Config) {
  val appConfig = config.getConfig("app")
  val actorSystemName = Try(appConfig.getString("actorsystem.name")).getOrElse("rest-ads")
}

object AppConfig {
  def apply() = new AppConfig(ConfigFactory.load())

  def apply(cnf: Config) = new AppConfig(cnf)

  def apply(resourceBaseName: String): AppConfig = {
    val cfg = ConfigFactory.load(resourceBaseName).withFallback(ConfigFactory.load())
    new AppConfig(cfg)
  }
}
