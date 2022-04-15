package com.digitalPlus.notification.common.messages

import zhttp.http.Request
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}


sealed trait Channel

case class SMS() extends Channel
case class EMAIL() extends Channel
case class WEBHOOK() extends Channel

case class Event(key: String, channel: Channel, payload: String)


object model {

  type WebHookRequest = WebHookMessage => Request

  case class SMSMessage(messageBody: String, from: String, to: String)

  implicit val smsMessageDecoder: JsonDecoder[SMSMessage] =
    DeriveJsonDecoder.gen[SMSMessage]

  implicit val smsMessageEncoder: JsonEncoder[SMSMessage] = DeriveJsonEncoder.gen[SMSMessage]

  case class EmailMessage(
                           from: String,
                           to: String,
                           subject: String,
                           body: String
                         )

  implicit val emailMessageDecoder: JsonDecoder[EmailMessage] =
    DeriveJsonDecoder.gen[EmailMessage]

  implicit val emailMessageEncoder: JsonEncoder[EmailMessage] = DeriveJsonEncoder.gen[EmailMessage]

  case class WebHookMessage(encodedPayload: String, host: String,  hookUrl: String, port: Int)

  implicit val webHookMessageDecoder: JsonDecoder[WebHookMessage] = DeriveJsonDecoder.gen[WebHookMessage]

  implicit val webHookMessageEncoder: JsonEncoder[WebHookMessage] = DeriveJsonEncoder.gen[WebHookMessage]

  implicit  val smsChannel: JsonEncoder[SMS] = DeriveJsonEncoder.gen[SMS]

  implicit  val emailChannel: JsonEncoder[EMAIL] = DeriveJsonEncoder.gen[EMAIL]

  implicit  val smsChannelDecoder: JsonDecoder[SMS] = DeriveJsonDecoder.gen[SMS]

  implicit  val emailChannelDecoder: JsonDecoder[EMAIL] = DeriveJsonDecoder.gen[EMAIL]

  implicit  val webHookDecoder: JsonDecoder[WEBHOOK] = DeriveJsonDecoder.gen[WEBHOOK]

  implicit  val webHookChannel: JsonEncoder[WEBHOOK] = DeriveJsonEncoder.gen[WEBHOOK]

  implicit  val channelEncoder: JsonEncoder[Channel] = DeriveJsonEncoder.gen[Channel]

  implicit  val channelDecoder: JsonDecoder[Channel] = DeriveJsonDecoder.gen[Channel]

  implicit val eventEncoder: JsonEncoder[Event] = DeriveJsonEncoder.gen[Event]

  implicit val eventDecoder: JsonDecoder[Event] = DeriveJsonDecoder.gen[Event]


}

object Error {
  case class DecodeError(str: String)               extends Error
  final case class InternalServerError(msg: String) extends Error
  final case class NotFound(msg: String)            extends Error
}