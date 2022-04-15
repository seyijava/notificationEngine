package com.digitalPlus.notification.engine.infrastructure.email

import com.digitalPlus.notification.common.messages.model.EmailMessage
import com.digitalPlus.notification.engine.config.MailServerConfig
import org.apache.commons.mail.SimpleEmail

import zio.{Accessible, Function1ToLayerOps, Task, URLayer, ZIO}
import org.apache.commons.mail.DefaultAuthenticator

trait EmailConnector {
  def sendEmail(emailMessage: EmailMessage): Task[String]
}

case class EmailConnectorLive(mailServerConfig: MailServerConfig)
    extends EmailConnector {
  override def sendEmail(emailMessage: EmailMessage): Task[String] =
    ZIO.log("Sending Email Notification") *> (for {
      messageId <- ZIO.attemptBlocking {
        val email = new SimpleEmail
        email.setHostName(mailServerConfig.host)
        email.setSmtpPort(mailServerConfig.port)
        email.setAuthenticator(
          new DefaultAuthenticator(
            mailServerConfig.username,
            mailServerConfig.password
          )
        )
        email.setSSLOnConnect(mailServerConfig.ssl)
        email.setFrom(emailMessage.from)
        email.setSubject(emailMessage.subject)
        email.setMsg(emailMessage.body)
        email.addTo(emailMessage.to)
        email.send()
      }
    } yield messageId)

}

object EmailConnector extends Accessible[EmailConnector] {
  val live: URLayer[MailServerConfig, EmailConnectorLive] =
    EmailConnectorLive.toLayer
}
