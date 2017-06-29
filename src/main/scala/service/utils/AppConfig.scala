package service.utils

import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.duration._
import scala.util.Try

/**
 * Project: rest-ads
 * Package: service.utils
 * Created by asoloviov on 6/28/17 6:42 PM.
 */
class AppConfig(val config: Config) {
  val appConfig = config.getConfig("app")
  val actorSystemName = Try(appConfig.getString("actorsystem.name")).getOrElse("rest-ads")
  private val httpConfig = appConfig.getConfig("http")
  val httpHost: String = Try(httpConfig.getString("host")).getOrElse("localhost")
  val httpPort: Int = Try(httpConfig.getInt("port")).getOrElse(9000)
  val allowedOriginOpt = Try(httpConfig.getString("cors.allowed-origin")).toOption
  val dbInitializationTimeout: FiniteDuration = Try(appConfig.getDuration("db.init.timeout", MILLISECONDS).millis).getOrElse(5.seconds)
}

object AppConfig {
  def apply() = new AppConfig(ConfigFactory.load())

  def apply(cnf: Config) = new AppConfig(cnf)

  def apply(resourceBaseName: String): AppConfig = {
    val cfg = ConfigFactory.load(resourceBaseName).withFallback(ConfigFactory.load())
    new AppConfig(cfg)
  }
}
