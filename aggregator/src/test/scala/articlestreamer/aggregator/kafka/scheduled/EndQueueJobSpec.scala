package articlestreamer.aggregator.kafka.scheduled

import articlestreamer.shared.BaseSpec
import articlestreamer.shared.kafka.KafkaProducerWrapper
import org.apache.kafka.clients.producer.ProducerRecord
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers._
import org.mockito.Mockito._
import org.quartz.{JobDataMap, JobDetail, JobExecutionContext}

class EndQueueJobSpec extends BaseSpec {

  "Executing job" should "send an end of queue message" in {
    val job = new EndQueueJob()

    val captor: ArgumentCaptor[ProducerRecord[String, String]] = ArgumentCaptor.forClass(classOf[ProducerRecord[String, String]])

    val producer = mock(classOf[KafkaProducerWrapper])
    when(producer.send(captor.capture())).thenReturn(null)
    val data = new JobDataMap()
    data.put("producer", producer)
    data.put("topic", "topic1")
    val jobDetails = mock(classOf[JobDetail])
    when(jobDetails.getJobDataMap).thenReturn(data)
    val context = mock(classOf[JobExecutionContext])
    when(context.getJobDetail).thenReturn(jobDetails)

    job.execute(context)

    verify(producer, times(1)).send(any())
    captor.getValue.key() shouldBe "endOfQueue"
    captor.getValue.value() shouldBe ""
  }

}
