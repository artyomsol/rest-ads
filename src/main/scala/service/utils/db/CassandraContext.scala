package service.utils.db

import com.datastax.driver.core._
import com.datastax.driver.core.policies.ExponentialReconnectionPolicy
import com.typesafe.config.Config

import scala.collection.JavaConverters._
import scala.concurrent.duration._
import scala.util.Try

/**
 * Project: rest-ads
 * Package: service.utils.db
 * Created by asoloviov on 6/28/17 6:33 PM.
 */
case class CassandraContext(session: Session, keyspace: String, defaultReadConsistency: String, defaultWriteConsistency: String) {
  def close(): Unit = session.getCluster.close()
}

object CassandraContext {

  def cassandraSessionForConfigPath(cnf: Config, path: String): CassandraContext = {
    val cassandraConfigPath = path + ".cassandra"
    val cassandraConfig = cnf.getConfig(cassandraConfigPath)
    val seeds = cassandraConfig.getStringList("seeds").asScala
    val port = Try(cassandraConfig.getInt("port")).getOrElse(9042)
    val poolingOptionsLocal = cassandraConfig.getIntList("pull.local").asScala.toArray
    assert(poolingOptionsLocal.length == 3, s"Config `$cassandraConfigPath.pull.local` must have 3 integer parameters")
    val poolingOptionsRemote = cassandraConfig.getIntList("pull.remote").asScala.toArray
    assert(poolingOptionsRemote.length == 3, s"Config `$cassandraConfigPath.pull.remote` must have 3 integer parameters")
    val heartbeatInterval: Int = Try(cassandraConfig.getDuration("pull.heartbeat.interval", SECONDS)).getOrElse(10L).toInt
    assert(heartbeatInterval > 0, s"Config value `$cassandraConfigPath.pull.heartbeat.interval` to large to be Integer in seconds")
    val socketConnectTimeout: Int = Try(cassandraConfig.getDuration("socket.connection.timeout", MILLISECONDS)).getOrElse(1000L).toInt
    assert(socketConnectTimeout > 0, s"Config value `$cassandraConfigPath.socket.connection.timeout` to large to be Integer in milliseconds")
    val socketReadTimeout: Int = Try(cassandraConfig.getDuration("socket.read.timeout", MILLISECONDS)).getOrElse(1000L).toInt
    assert(socketReadTimeout > 0, s"Config value `$cassandraConfigPath.socket.read.timeout` to large to be Integer in milliseconds")
    val reconnectionMinInterval: Int = cassandraConfig.getDuration("reconnection.min.interval", MILLISECONDS).toInt
    assert(reconnectionMinInterval > 0, s"Config value `$cassandraConfigPath.reconnection.min.interval` to large to be Integer in milliseconds")
    val reconnectionMaxInterval: Int = cassandraConfig.getDuration("reconnection.max.interval", MILLISECONDS).toInt
    assert(reconnectionMaxInterval > 0, s"Config value `$cassandraConfigPath.reconnection.max.interval` to large to be Integer in milliseconds")
    val compression: String = Try(cassandraConfig.getString("compression")).getOrElse("lz4")
    assert(compression == "lz4" || compression == "none", s"Config value `$cassandraConfigPath.compression` must be either 'lz4' or 'none'")
    val fetchSize: Int = Try(cassandraConfig.getInt("fetch.size")).getOrElse(5000)
    assert(fetchSize > 0, s"Config value `$cassandraConfigPath.fetch.size` must be > 0")
    val loggerTimeTreshold: Long = Try(cassandraConfig.getDuration("logger.time.treshold", MILLISECONDS)).getOrElse(QueryLogger.DEFAULT_SLOW_QUERY_THRESHOLD_MS)
    val loggerMaxQueryStringLength: Int = Try(cassandraConfig.getInt("logger.query.length.max")).getOrElse(QueryLogger.DEFAULT_MAX_QUERY_STRING_LENGTH)
    val keySpace: String = cassandraConfig.getString("keyspace")
    val consistencyLevels = ConsistencyLevel.values().map(_.name())
    val readConsistency: String = cassandraConfig.getString("consistency.read")
    assert(consistencyLevels.contains(readConsistency), s"Config value `$cassandraConfigPath.consistency.read` must be one of:" + consistencyLevels.mkString(","))
    val writeConsistency: String = cassandraConfig.getString("consistency.write")
    assert(consistencyLevels.contains(writeConsistency), s"Config value `$cassandraConfigPath.consistency.write` must be one of:" + consistencyLevels.mkString(","))
    val usernameOpt: Option[String] = Try(cassandraConfig.getString("username")).toOption
    val passwordOpt: Option[String] = Try(cassandraConfig.getString("password")).toOption
    val authProvider = (for {
      username <- usernameOpt
      password <- passwordOpt
    } yield new PlainTextAuthProvider(username, password)).getOrElse(AuthProvider.NONE)
    var clusterBuilder = Cluster.builder().withAuthProvider(authProvider)
    seeds.foreach(point => clusterBuilder = clusterBuilder.addContactPoint(point))
    clusterBuilder =
      clusterBuilder.withPort(port)
        .withPoolingOptions(
          new PoolingOptions()
            .setPoolTimeoutMillis(0) //if a connection pool is busy, the driver will not block, just move to the next host immediately.
            .setConnectionsPerHost(HostDistance.REMOTE, poolingOptionsRemote(0), poolingOptionsRemote(1))
            .setNewConnectionThreshold(HostDistance.REMOTE, poolingOptionsRemote(2))
            .setConnectionsPerHost(HostDistance.LOCAL, poolingOptionsLocal(0), poolingOptionsLocal(1))
            .setNewConnectionThreshold(HostDistance.LOCAL, poolingOptionsLocal(2))
            .setHeartbeatIntervalSeconds(heartbeatInterval)
        )
        .withSocketOptions(
          new SocketOptions()
            .setConnectTimeoutMillis(socketConnectTimeout)
            .setReadTimeoutMillis(socketReadTimeout)
        )
        .withReconnectionPolicy(
          new ExponentialReconnectionPolicy(reconnectionMinInterval, reconnectionMaxInterval)
        )
        .withQueryOptions(new QueryOptions().setFetchSize(fetchSize))
        .withCompression(
          compression match {
            case "none" => ProtocolOptions.Compression.NONE
            case _ => ProtocolOptions.Compression.LZ4
          }
        )
    val cluster = clusterBuilder.build()
    val queryLogger = QueryLogger.builder()
      .withConstantThreshold(loggerTimeTreshold)
      .withMaxQueryStringLength(loggerMaxQueryStringLength)
      .build()
    cluster.register(queryLogger)
    new CassandraContext(cluster.connect(), keySpace, readConsistency, writeConsistency)
  }
}
