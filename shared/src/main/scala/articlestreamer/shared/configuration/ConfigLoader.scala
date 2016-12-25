package articlestreamer.shared.configuration

import java.io.{File, PrintWriter}

import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.LazyLogging

import scala.collection.JavaConversions._

final case class TwitterSearchConfig(mainTag: String,
                               minimumScore: Int,
                               relatedTags: List[String],
                               articleCloseWords: List[String],
                               articleRelatedWords: List[String],
                               subjectCloseWords: List[String],
                               subjectRelatedWords: List[String],
                               articleUnrelatedWords: List[String],
                               subjectUnrelatedWords: List[String])

final case class MysqlConfig(jdbcUrl: String, user: String, password: String, driver: String)

final case class RedisConfig(host: String, port: Int, expiryTime: Long)

final case class TwitterAuthConfig(consumerKey: String,
                                   consumerSecret: String,
                                   accessToken: String,
                                   accessSecret: String)

/**
  * Configuration for Twitter (used through Twitter4J)
  * @param authConfig : OAuth keys and secrets
  * @param searchConfig : Tags and scores evaluation criteria
  * @param batchSize : Size of the batch required from the Twitter API when requesting tweets details
  * @param ignoredAuthors : list of authors username whose tweets should be ignored
  */
final case class TwitterConfig(authConfig: TwitterAuthConfig,
                               searchConfig: TwitterSearchConfig,
                               batchSize: Int, ignoredAuthors: List[String])

trait ConfigLoader extends LazyLogging with Serializable {

  protected val appConfig: Config = ConfigFactory.load()

  private val twitterAuthConfig: TwitterAuthConfig = TwitterAuthConfig(
    appConfig.getString("twitter.oauth.oauthConsumerKey"),
    appConfig.getString("twitter.oauth.oauthConsumerSecret"),
    appConfig.getString("twitter.oauth.oauthAccessToken"),
    appConfig.getString("twitter.oauth.oauthAccessTokenSecret")
  )
  private val twitterPath = "twitter.search"
  private val twitterSearchConfig = TwitterSearchConfig(
    appConfig.getString(s"$twitterPath.tagToTrack"),
    appConfig.getInt(s"$twitterPath.minimumScore"),
    appConfig.getStringList(s"$twitterPath.relatedTags").toList,
    appConfig.getStringList(s"$twitterPath.articleCloseWords").toList,
    appConfig.getStringList(s"$twitterPath.articleRelatedWords").toList,
    appConfig.getStringList(s"$twitterPath.subjectCloseWords").toList,
    appConfig.getStringList(s"$twitterPath.subjectRelatedWords").toList,
    appConfig.getStringList(s"$twitterPath.articleUnrelatedWords").toList,
    appConfig.getStringList(s"$twitterPath.subjectUnrelatedWords").toList
  )
  val twitterConfig: TwitterConfig = TwitterConfig(
    twitterAuthConfig,
    twitterSearchConfig,
    appConfig.getInt("twitter.tweetsBatchSize"),
    appConfig.getStringList("twitter.ignoredAuthors").toList)

  val mysqlConfig = MysqlConfig(
    appConfig.getString("mysql.jdbcUrl"),
    appConfig.getString("mysql.user"),
    appConfig.getString("mysql.password"),
    appConfig.getString("mysql.driver")
  )

  val redisConfig = RedisConfig(
    appConfig.getString("redis.host"),
    appConfig.getInt("redis.port"),
    appConfig.getLong("redis.expiryTime")
  )

  /**
    * Size of the tweets batch when querying for tweet info
    */

  val kafkaMainTopic = appConfig.getString("kafka.topic-default")
  val kafkaFirstTopic = appConfig.getString("kafka.topic1")
  val kafkaSecondTopic = appConfig.getString("kafka.topic2")
  val kafkaArticlesTopic = appConfig.getString("kafka.topic-articles")

  val kafkaBrokers = appConfig.getString("kafka.brokers")
  /**
    * Maximum polling attempts before stopping
    */
  val kafkaMaxAttempts = appConfig.getInt("kafka.maxAttempts")

  val kafkaSSLMode = appConfig.getBoolean("kafka.sslProtocol")
  var kafkaTrustStore = ""

  protected def setupTrustStore(kafkaTrustStore: String) = {

    val kafkaTrustStore = appConfig.getString("kafka.security.storeLocation")

    //val localDir = Paths.get(".").toAbsolutePath.normalize().toString
    val caFilePath = s"$kafkaTrustStore/ca.pem"

    //Create the directories in path if necessary
    val file = new File(caFilePath)
    file.getParentFile.mkdirs()

    val caWriter = new PrintWriter(caFilePath, "UTF-8")
    val ca = appConfig.getString("kafka.security.authority")
    caWriter.println(ca)
    caWriter.close()

    val certWriter = new PrintWriter(s"$kafkaTrustStore/cert.pem", "UTF-8")
    val cert = appConfig.getString("kafka.security.certificate")
    certWriter.println(cert)
    certWriter.close()

    val keyWriter = new PrintWriter(s"$kafkaTrustStore/key.pem", "UTF-8")
    val privateKey = appConfig.getString("kafka.security.privateKey")
    keyWriter.println(privateKey)
    keyWriter.close()

    exec(s"openssl pkcs12 -export -password pass:test1234 -out $kafkaTrustStore/store.pkcs12 -inkey $kafkaTrustStore/key.pem -certfile $kafkaTrustStore/ca.pem -in $kafkaTrustStore/cert.pem -caname 'CARoot' -name client")(x => logger.debug(x))

    exec(s"keytool -importkeystore -noprompt -srckeystore $kafkaTrustStore/store.pkcs12 -destkeystore $kafkaTrustStore/keystore.jks -srcstoretype pkcs12 -srcstorepass test1234 -srckeypass test1234 -destkeypass test1234 -deststorepass test1234 -alias client")(x => logger.debug(x))

    exec(s"keytool -noprompt -keystore $kafkaTrustStore/truststore.jks -alias CARoot -import -file $kafkaTrustStore/ca.pem -storepass test1234")(x => logger.debug(x))

  }

  def exec(cmd : String)(func : String=>Unit) : Unit = {
    val commands = cmd.split(" ")
    val proc = new ProcessBuilder(commands: _*).redirectErrorStream(true).start()
    val ins = new java.io.BufferedReader(new java.io.InputStreamReader(proc.getInputStream))

    //spin off a thread to read process output.
    val outputReaderThread = new Thread(new Runnable(){
      def run() : Unit = {
        var ln : String = null
        while({ln = ins.readLine; ln != null})
          func(ln)
      }
    })
    outputReaderThread.start()

    //suspense this main thread until sub process is done.
    proc.waitFor

    //wait until output is fully read/completed.
    outputReaderThread.join()

    ins.close()
  }

}
