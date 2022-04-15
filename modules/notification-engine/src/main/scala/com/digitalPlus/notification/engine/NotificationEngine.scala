package com.digitalPlus.notification.engine

import com.digitalPlus.notification.engine.config.{AppConfig, MailServerConfig, TwilioConfig}
import com.digitalPlus.notification.engine.handler.NotificationEventHandler
import com.digitalPlus.notification.engine.infrastructure.email.{EmailConnector, EmailConnectorLive}
import com.digitalPlus.notification.engine.infrastructure.kafka.NotificationEventBus
import com.digitalPlus.notification.engine.infrastructure.kafka.NotificationEventBus.EventHandlers
import com.digitalPlus.notification.engine.infrastructure.sms.{SMSConnector, SMSConnectorLive}
import com.digitalPlus.notification.engine.infrastructure.webhook.{HttpClientAdapter, WebHookConnector}
import zio.kafka.consumer.{Consumer, ConsumerSettings}
import zio.{Clock, Scope, Task, TaskLayer, ZEnv, ZIO, ZIOAppArgs, ZIOAppDefault, ZLayer}

object NotificationEngine extends ZIOAppDefault {

  val mailLConfigLayer: TaskLayer[MailServerConfig] = {
    ZIO
      .serviceWith[AppConfig](_.mailServerConfig)
      .provide(config.live)
      .flatMap(mailConfig => Task(mailConfig))
      .toLayer
  }

  val twilioConfig: TaskLayer[TwilioConfig] = {
    ZIO
      .serviceWith[AppConfig](_.twilioConfig)
      .provide(config.live)
      .flatMap(smsConfig => Task(smsConfig))
      .toLayer
  }

  val smsConnectorLayer: ZLayer[Any, Throwable, SMSConnectorLive] =
    twilioConfig >>> SMSConnector.live

  val mailConnectorLayer: ZLayer[Any, Throwable, EmailConnectorLive] =
    mailLConfigLayer >>> EmailConnector.live

  val webHookConnectorLayer = HttpClientAdapter.live >>> WebHookConnector.live

  val notificationHandlerLayer =
    (smsConnectorLayer ++ mailConnectorLayer ++ webHookConnectorLayer) >>> NotificationEventHandler.live

  val notificationEventHandler: EventHandlers = event =>
    NotificationEventHandler(_.handleEvent(event))
      .provide(notificationHandlerLayer, Clock.live)

  override def run: ZIO[ZEnv with ZIOAppArgs with Scope, Any, Any] = {
    import com.digitalPlus.notification.common.messages.model.eventDecoder
    for {
      kafkaConfig <-
        ZIO.serviceWith[AppConfig](_.kafkaConsumerConfig).provide(config.live)
      consumerSettings = ConsumerSettings(List(kafkaConfig.bootstrapServer))
        .withGroupId(kafkaConfig.groupId)
        .withProperty("auto.offset.reset", "earliest")
      consumer = Consumer.make(consumerSettings).orDie
      _ <- NotificationEventBus(
        _.start(
          kafkaConfig.topic,
          kafkaConfig.parSize,
          notificationEventHandler
        )
      ).provide(ZLayer.scope, consumer.toLayer, Clock.live, NotificationEventBus.live)

    } yield ()
  }
}
