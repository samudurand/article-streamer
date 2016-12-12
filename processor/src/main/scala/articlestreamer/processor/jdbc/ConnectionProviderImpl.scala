package articlestreamer.processor.jdbc
import java.sql.{Connection, DriverManager}
import java.util.Properties

class ConnectionProviderImpl extends ConnectionProvider {

  override def getConnection(url: String, info: Properties): Connection = {
    DriverManager.getConnection(url, info)
  }

}
