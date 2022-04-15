package com.digitalPlus.notification.engine

import zio.TaskLayer
import zio.config.magnolia.descriptor
import zio.config.typesafe.TypesafeConfig
import zio.TaskLayer
import zio.config.ConfigDescriptor._
import zio.config.magnolia.descriptor
import zio.config.syntax._
import zio.config.typesafe.TypesafeConfig

package object config {

  case class TwilioConfig(account: String, token: String)

  case class MailServerConfig(
      host: String,
      port: Int,
      username: String,
      password: String,
      ssl: Boolean
  )

  case class KafkaConsumerConfig(
      bootstrapServer: String,
      topic: String,
      groupId: String,
      parSize: Int
  )

  case class SPConfig(host: String, port: Int, baseUrl: String, token: String)

  case class AppConfig(
      kafkaConsumerConfig: KafkaConsumerConfig,
      spConfig: SPConfig,
      mailServerConfig: MailServerConfig,
      twilioConfig: TwilioConfig
  )

  type AllConfig = AppConfig
    with KafkaConsumerConfig
    with SPConfig
    with MailServerConfig

  final val Root = "alert"

  private final val Descriptor = descriptor[AppConfig]

  val appConfig = TypesafeConfig.fromResourcePath(nested(Root)(Descriptor))

  val live: TaskLayer[AllConfig] =
    appConfig >+>
      appConfig.narrow(_.spConfig) >+>
      appConfig.narrow(_.mailServerConfig) >+>
      appConfig.narrow(_.twilioConfig) >+>
      appConfig.narrow(_.kafkaConsumerConfig)
}
