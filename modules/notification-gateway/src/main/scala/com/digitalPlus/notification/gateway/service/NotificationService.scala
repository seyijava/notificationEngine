package com.digitalPlus.notification.gateway.service

import com.digitalPlus.notification.common.messages.model.{EmailMessage, SMSMessage, WebHookMessage, emailMessageEncoder, smsMessageEncoder,webHookMessageEncoder}
import com.digitalPlus.notification.common.messages.{Channel, EMAIL, Event, SMS, WEBHOOK}
import com.digitalPlus.notification.gateway.kafka.EventPublisher
import zio.json._
import zio.{Accessible, Random, Task, URLayer}

import java.util.Base64

trait NotificationService {
  def sendEmailEvent(emailMessage: EmailMessage) : Task[Unit]
   def sendSMSEvent(smsMessage: SMSMessage) : Task[Unit]
   def sendWebHookEvent(webHookMessage: WebHookMessage) : Task[Unit]
}


case class NotificationServiceLive(eventPublisher: EventPublisher,random: Random) extends NotificationService {
  override def sendEmailEvent(emailMessage: EmailMessage): Task[Unit] = {
    publishEvent[EmailMessage](emailMessage,EMAIL())
  }

  override def sendSMSEvent(smsMessage: SMSMessage): Task[Unit] ={
     publishEvent[SMSMessage](smsMessage,SMS())
  }


  override def sendWebHookEvent(webHookMessage: WebHookMessage): Task[Unit] =
  {
    val webHookMsg = webHookMessage.copy(encodedPayload = new String(Base64.getDecoder.decode(webHookMessage.encodedPayload)))
    publishEvent[WebHookMessage](webHookMsg,WEBHOOK())
  }

    def publishEvent[E](msg: E, channel: Channel)(implicit msgEncoder: JsonEncoder[E]) : Task[Unit] =
     for {
       eventId <- random.nextUUID
      event = Event(key = eventId.toString, payload = msg.toJson,channel=channel)
       _<- eventPublisher.publishEvent(event)
     } yield ()

}


object NotificationService extends Accessible[NotificationService]{
    val live: URLayer[EventPublisher with Random, NotificationServiceLive] = NotificationServiceLive.toLayer
}