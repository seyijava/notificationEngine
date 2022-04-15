package com.digitalPlus.notification.engine.infrastructure.kafka

import com.digitalPlus.notification.common.messages.{Channel, Event}
import com.digitalPlus.notification.engine.infrastructure.kafka.NotificationEventBus.EventHandlers
import zio.json.{DeriveJsonDecoder, JsonDecoder}
import zio.kafka.consumer.{Consumer, Offset}
import zio.kafka.consumer.Subscription.Topics
import zio.kafka.serde.Serde
import zio.{Accessible, Clock, Task, ZIO}

import scala.util.{Failure, Success, Try}

trait NotificationEventBus {

  def start(topic: String, parSize: Int, eventHandler: EventHandlers)(implicit
      jsonDecoder: JsonDecoder[Event]
  ): ZIO[Clock, Throwable, Unit]
}

case class NotificationEventBusLive(consumer: Consumer) extends NotificationEventBus {
  override def start(topic: String, parSize: Int, eventHandler: EventHandlers)(
      implicit jsonDecoder: JsonDecoder[Event]
  ): ZIO[Clock, Throwable, Unit] =
    ZIO.log("Starting consuming Notification Events") *>
      consumer
        .subscribeAnd(Topics(Set(topic)))
        .plainStream(Serde.string, Serde.string.asTry)
        .mapZIOPar(parSize) { record =>
          val tryValue: Try[String] = record.record.value()
          val offset: Offset = record.offset
          tryValue match {
            case Success(value) =>
              jsonDecoder.decodeJson(value) match {
                case Right(event) =>
                  eventHandler(event)
                    .flatMap {
                      case Right(_) =>
                        ZIO.log("Notification Event Process Successfully")
                      case Left(ex) =>
                        ZIO.log(s"Error Processing Message ${ex}")
                    }
                    .as(offset)
                case Left(_) => ZIO.log("Failed").as(offset)
              }
            case Failure(ex) =>
              ZIO.log(ex.getMessage).as(offset)
          }
        }
        .aggregateAsync(Consumer.offsetBatches)
        .mapZIO(_.commit)
        .runDrain
}

object NotificationEventBus extends Accessible[NotificationEventBus] {

  val live = NotificationEventBusLive.toLayer

  type EventHandlers = Event => Task[Either[String, Unit]]

}
