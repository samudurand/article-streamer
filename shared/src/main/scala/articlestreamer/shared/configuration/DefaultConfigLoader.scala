package articlestreamer.shared.configuration

import java.io.{File, PrintWriter}

import com.typesafe.config.ConfigFactory

class DefaultConfigLoader extends ConfigLoader {

  // Setup ssl config if needed
  if (kafkaSSLMode) {
    kafkaTrustStore = appConfig.getString("kafka.security.storeLocation")
    setupTrustStore(kafkaTrustStore)
  }

}
