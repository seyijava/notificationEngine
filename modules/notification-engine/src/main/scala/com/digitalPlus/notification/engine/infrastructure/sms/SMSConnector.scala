package com.digitalPlus.notification.engine.infrastructure.sms

import com.digitalPlus.notification.common.messages.model.SMSMessage
import com.digitalPlus.notification.engine.config.TwilioConfig
import com.twilio.Twilio
import zio.{Accessible, Function1ToLayerOps, Task, URLayer, ZIO}
import com.twilio.rest.api.v2010.account.Message
import com.twilio.`type`.PhoneNumber

trait SMSConnector {
  def sendSMS(smsMessage: SMSMessage): Task[String]
}

case class SMSConnectorLive(twilioConfig: TwilioConfig) extends SMSConnector {

  override def sendSMS(smsMsg: SMSMessage): Task[String] =
    ZIO.log(s"Sending SMS Notification $smsMsg") *> (for {
      msgId <- ZIO.attemptBlocking {
        Twilio.init(twilioConfig.account, twilioConfig.token)
        val message = Message
          .creator(
            new PhoneNumber(smsMsg.to),
            new PhoneNumber(smsMsg.from),
            smsMsg.messageBody
          )
          .create()
        message.getSid
      }
    } yield msgId)
}

object SMSConnector extends Accessible[SMSConnector] {
  val live: URLayer[TwilioConfig, SMSConnectorLive] = SMSConnectorLive.toLayer
}
