package articlestreamer.processor.spark
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

class OnDemandSparkProvider extends SparkProvider {

  val config = new SparkConf()
    .setAppName("Spark App")
    .setMaster("local[2]")
    .set("spark.streaming.stopGracefullyOnShutdown","true")

  override def getSparkConf(): SparkConf = {
    config
  }

  override def getSparkSession(): SparkSession = {
    val sparkSession = SparkSession
      .builder()
      .config(config)
      .getOrCreate()

    sys.addShutdownHook(sparkSession.stop())

    sparkSession
  }

}
