package articlestreamer.shared.configuration

import java.io.{File, PrintWriter}

import com.typesafe.config.ConfigFactory

object ConfigLoader {

  private val appConfig = ConfigFactory.load()

  setupTrustStore()

  val twitterOuathConsumerKey = appConfig.getString("twitter.oauth.oauthConsumerKey")
  val twitterOauthConsumerSecret = appConfig.getString("twitter.oauth.oauthConsumerSecret")
  val twitterOauthAccessToken = appConfig.getString("twitter.oauth.oauthAccessToken")
  val twitterOauthAccessTokenSecret = appConfig.getString("twitter.oauth.oauthAccessTokenSecret")

  val kafkaMainTopic = appConfig.getString("kafka.topic")

  private def setupTrustStore() = {

    val trustStoreLocation = appConfig.getString("kafka.security.storeLocation")

    val caFilePath = s"$trustStoreLocation/ca.pem"

    //Create the directories in path if necessary
    val file = new File(caFilePath)
    file.getParentFile.mkdirs()

    val caWriter = new PrintWriter(caFilePath, "UTF-8")
    val ca = appConfig.getString("kafka.security.authority")
    caWriter.println(ca)
    caWriter.close()

    val certWriter = new PrintWriter(s"$trustStoreLocation/cert.pem", "UTF-8")
    val cert = appConfig.getString("kafka.security.certificate")
    certWriter.println(cert)
    certWriter.close()

    val keyWriter = new PrintWriter(s"$trustStoreLocation/key.pem", "UTF-8")
    val privateKey = appConfig.getString("kafka.security.privateKey")
    keyWriter.println(privateKey)
    keyWriter.close()

    val r = Runtime.getRuntime
    val p: Process = r.exec(s"openssl pkcs12 -export -password pass:test1234 -out $trustStoreLocation/store.pkcs12 -inkey $trustStoreLocation/key.pem -certfile $trustStoreLocation/ca.pem -in $trustStoreLocation/cert.pem -caname 'CA Root' -name client")
    p.waitFor()

    val p2 = r.exec(s"keytool -importkeystore -noprompt -srckeystore $trustStoreLocation/store.pkcs12 -destkeystore $trustStoreLocation/keystore.jks -srcstoretype pkcs12 -srcstorepass test1234 -srckeypass test1234 -destkeypass test1234 -deststorepass test1234 -alias client")
    p2.waitFor()

    val p3 = r.exec(s"keytool -noprompt -keystore $trustStoreLocation/truststore.jks -alias CARoot -import -file $trustStoreLocation/ca.pem -storepass test1234")
    p3.waitFor()

  }
}
