package com.digitalPlus.notification.gateway.kafka

import com.digitalPlus.notification.common.messages.Event
import zio.json.{JsonEncoder, _}
import zio.kafka.producer.Producer
import zio.kafka.serde.Serde
import zio.{Accessible, Task, ZIO, _}
import com.digitalPlus.notification.common.messages.model.eventEncoder
import zio.json._

trait EventPublisher  {
  def publishEvent( m: Event): Task[Unit]
}
case class EventPublisherLive(producer: Producer, topic: String) extends EventPublisher {
  override def publishEvent(event: Event): Task[Unit] = {
     ZIO.log(s"Publishing Event Payload [$event]")  *> (for{
         _   <- producer.produce(topic, event.key,event.toJson,Serde.string,Serde.string)

       } yield ())
  }
}

object EventPublisher extends Accessible[EventPublisher]
{
       val live: URLayer[Producer with String, EventPublisherLive] = EventPublisherLive.toLayer
}


