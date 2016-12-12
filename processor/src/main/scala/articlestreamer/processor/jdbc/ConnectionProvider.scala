package articlestreamer.processor.jdbc

import java.sql.Connection
import java.util.Properties

trait ConnectionProvider extends Serializable {

  def getConnection(url: String, info: Properties): Connection

}
