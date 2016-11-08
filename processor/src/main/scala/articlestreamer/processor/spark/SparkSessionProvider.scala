package articlestreamer.processor.spark

import org.apache.spark.sql.SparkSession

trait SparkSessionProvider {

  def getSparkSession(): SparkSession

}
