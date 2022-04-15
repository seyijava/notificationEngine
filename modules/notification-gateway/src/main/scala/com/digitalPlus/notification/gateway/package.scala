package com.digitalPlus.notification

import zio.TaskLayer
import zio.config.ConfigDescriptor._
import zio.config.magnolia.descriptor
import zio.config.syntax._
import zio.config.typesafe.TypesafeConfig

package object gateway {

  case class TwilioConfig(account: String, token: String)

  case class AppServerConfig(
                               host: String,
                               port: Int,
                             )

  case class KafkaProducerConfig(
                                  bootstrapServer: String,
                                  topic: String,
                                )

  case class SPConfig(host: String, port: Int, baseUrl: String, token: String)

  case class AppConfig(
                        kafkaProducerConfig: KafkaProducerConfig,
                        appServerConfig: AppServerConfig
                      )

  type AllConfig = AppConfig
    with KafkaProducerConfig
    with AppServerConfig


  final val Root = "gateway"

  private final val Descriptor = descriptor[AppConfig]

  val appConfig = TypesafeConfig.fromResourcePath(nested(Root)(Descriptor))

  val live: TaskLayer[AllConfig] =
    appConfig >+>
      appConfig.narrow(_.appServerConfig) >+>
      appConfig.narrow(_.kafkaProducerConfig)


}
