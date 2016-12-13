package articlestreamer.processor.spark

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

trait SparkProvider {

  def getSparkConf(): SparkConf

  def getSparkSession(): SparkSession

}
