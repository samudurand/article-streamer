package articlestreamer.aggregator.kafka.scheduled

import articlestreamer.aggregator.kafka.KafkaProducerWrapper
import articlestreamer.shared.Constants
import org.apache.kafka.clients.producer.ProducerRecord
import org.quartz.{Job, JobExecutionContext}


class EndQueueJob(producer: KafkaProducerWrapper, topic: String) extends Job {

  override def execute(context: JobExecutionContext) = {
    producer.send(endOfQueueRecord())
  }

  private def endOfQueueRecord(): ProducerRecord[String, String] = {
    new ProducerRecord[String, String](topic, Constants.END_OF_QUEUE_KEY, "")
  }
}
