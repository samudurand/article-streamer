package articlestreamer.aggregator.kafka.scheduled

import articlestreamer.aggregator.kafka.KafkaProducerWrapper
import articlestreamer.shared.Constants
import org.apache.kafka.clients.producer.ProducerRecord
import org.quartz.{Job, JobExecutionContext}


class EndQueueJob extends Job {

  val TOPIC_PARAM = "topic"
  val PRODUCER_PARAM = "producer"

  override def execute(context: JobExecutionContext) = {
    val params = context.getJobDetail.getJobDataMap
    val topic = params.getString(TOPIC_PARAM)
    val producer = (params.get(PRODUCER_PARAM)).asInstanceOf[KafkaProducerWrapper]
    producer.send(endOfQueueRecord(topic))
  }

  private def endOfQueueRecord(topic: String): ProducerRecord[String, String] = {
    new ProducerRecord[String, String](topic, Constants.END_OF_QUEUE_KEY, "")
  }
}
