package com.digitalPlus.notification.engine.handler

import com.digitalPlus.notification.common.messages.model.{EmailMessage, SMSMessage, WebHookMessage, webHookMessageDecoder}
import com.digitalPlus.notification.common.messages.{EMAIL, Event, SMS, WEBHOOK}
import com.digitalPlus.notification.engine.infrastructure.email.EmailConnector
import com.digitalPlus.notification.engine.infrastructure.kafka.EventHandler
import com.digitalPlus.notification.engine.infrastructure.sms.SMSConnector
import com.digitalPlus.notification.engine.infrastructure.webhook.WebHookConnector.webHookRequest
import com.digitalPlus.notification.engine.infrastructure.webhook.{HttpClientAdapter, WebHookConnector}
import zio.json.JsonDecoder
import zio.{Accessible, Function3ToLayerOps, Task, URLayer, ZIO}

trait NotificationEventHandler extends EventHandler

case class NotificationHandlerLive(
    emailConnector: EmailConnector,
    smsConnector: SMSConnector,
    webHookConnector: WebHookConnector
) extends NotificationEventHandler {

  override def handleEvent(event: Event): Task[Either[String, Unit]] = {
    event.channel match {
      case EMAIL() =>
        parseJsonPayload[EmailMessage](event.payload) match {
          case Left(decodeError) => ZIO.left(decodeError)
          case Right(emailMsg) =>
            processResponse(emailConnector.sendEmail(emailMsg))
        }
      case SMS() =>
        parseJsonPayload[SMSMessage](event.payload) match {
          case Left(decodeError) => ZIO.left(decodeError)
          case Right(smsMessage) =>
            processResponse[String](smsConnector.sendSMS(smsMessage))
        }
      case WEBHOOK() =>
        parseJsonPayload[WebHookMessage](event.payload) match {
          case Left(decodeError) => ZIO.left(decodeError)
          case Right(webHookMsg) =>
            webHookConnector
              .sendWebHookNotification(webHookMsg)(webHookRequest)
              .provideLayer(HttpClientAdapter.live)

        }
      case _ => ZIO.left("Channel Not defined")
    }
  }

  def parseJsonPayload[P](
      payload: String
  )(implicit jsonDecoder: JsonDecoder[P]): Either[String, P] = {
    jsonDecoder.decodeJson(payload)
  }

  def processResponse[R](result: Task[R]): Task[Either[String, Unit]] = {
    result.fold(failure => Left(failure.getMessage), _ => Right())
  }

}

object NotificationEventHandler extends Accessible[NotificationEventHandler] {
  val live: URLayer[
    EmailConnector with SMSConnector with WebHookConnector,
    NotificationEventHandler
  ] = NotificationHandlerLive.toLayer
}
