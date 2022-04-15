package com.digitalPlus.notification.gateway

import com.digitalPlus.notification.gateway
import com.digitalPlus.notification.gateway.api.NotificationAPIGateway
import com.digitalPlus.notification.gateway.kafka.EventPublisher
import com.digitalPlus.notification.gateway.service.NotificationService
import zhttp.http.Http
import zhttp.service.Server
import zio.Console.{printLine, readLine}
import zio.kafka.producer.{Producer, ProducerSettings}
import zio.{Console, Random, Scope, ZEnv, ZIO, ZIOAppArgs, ZIOAppDefault, ZLayer}
import java.io.IOException

object NotificationAPIGatewayServer extends ZIOAppDefault{

  val appStart: ZIO[Console with AppConfig, IOException, Unit] = for{
   kafkaConfig <- ZIO.serviceWith[AppConfig](_.kafkaProducerConfig)
   topicLayer = ZIO.succeed(kafkaConfig.topic) .toLayer
    producerSetting: ProducerSettings = ProducerSettings(List(kafkaConfig.bootstrapServer))
    producer = Producer.make(producerSetting)
   serverConfig <- ZIO.serviceWith[AppConfig](_.appServerConfig)
    _         <- ZIO.log("Starting Notification Gateway  Server")
    f        <- Server.start(serverConfig.port, NotificationAPIGateway.apiRoute.withAccessControlAllowOrigin("*") <> Http.notFound)
      .provide(producer.toLayer,NotificationService.live,EventPublisher.live,topicLayer,ZLayer.scope,Random.live)
      .forkDaemon
    _        <- printLine("Press Any Key to stop the server") *> readLine.catchAll(e =>
      printLine(s"There was an error server can't be started !!! ${e.getMessage}")
    ) *> f.interrupt
  }yield ()


  override def run: ZIO[ZEnv with ZIOAppArgs with Scope, Any, Any] = appStart.provide(Console.live,gateway.live)
}
