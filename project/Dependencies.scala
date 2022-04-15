import sbt._

object Dependencies {

  val ZioVersion = "2.0.0-RC2"
  val elasticV = "7.15.0"

  private object Versions {
    val zio = "2.0.0-RC3"
    val zioKafka = "2.0.0-M2"
    val zioConfig = "3.0.0-RC6"
    val zioLogging = "2.0.0-RC2"
    val zioLog4j = "2.0.0-RC5"
    val circe = "0.13.0"
    val sttp = "2.2.9"
    val log4j = "2.13.3"
    val disruptor = "3.4.2"
    val jackson = "2.12.0"
    val kafka = "2.4.1.1"
    val zioInteropCats = "2.3.1.0"
    val ZioHttp = "2.0.0-RC5"
    val ZioJson = "0.3.0-RC5"
    val ZioPrelude = "1.0.0-RC10"
    val http4s = "0.22.11"
    final val ZioQuill = "3.17.0-RC2"
    final val PostgresSql = "42.3.2"
    final val Flyway = "8.5.1"
    final val ZioConfig = "3.0.0-RC2"
  }

  object Libraries {
    val zio = Seq(
      "dev.zio" %% "zio" % Versions.zio,
      "dev.zio" %% "zio-streams" % Versions.zio,
      "dev.zio" %% "zio-macros" % Versions.zio,
      "dev.zio" %% "zio-json" % Versions.ZioJson,
      "io.d11" %% "zhttp" % Versions.ZioHttp,
      "dev.zio" %% "zio-prelude" % Versions.ZioPrelude,
      "dev.zio" %% "zio-config" % Versions.ZioConfig,
      "dev.zio" %% "zio-config-typesafe" % Versions.ZioConfig,
      "dev.zio" %% "zio-config-magnolia" % Versions.ZioConfig
    )

    val zioKafka = Seq(
      "dev.zio" %% "zio" % Versions.zio,
      "dev.zio" %% "zio-streams" % Versions.zio,
      "dev.zio" %% "zio-kafka" % Versions.zioKafka,
      "dev.zio" %% "zio-json" % Versions.ZioJson
    )

    lazy val zioConfig = Seq(
      "dev.zio" %% "zio-config-magnolia" % Versions.zioConfig,
      "dev.zio" %% "zio-config-typesafe" % Versions.zioConfig
    )

    val zioLogging = Seq(
      "dev.zio" %% "zio-logging" % Versions.zioLogging,
      "dev.zio" %% "zio-logging-slf4j" % Versions.zioLog4j
    )

    val circe = Seq(
      "io.circe" %% "circe-core" % Versions.circe,
      "io.circe" %% "circe-generic" % Versions.circe,
      "io.circe" %% "circe-parser" % Versions.circe
    )

    val sttp = Seq(
      "com.softwaremill.sttp.client" %% "async-http-client-backend-zio" % Versions.sttp,
      "com.softwaremill.sttp.client" %% "circe" % Versions.sttp
    )

    val log4j = Seq(
      //"org.apache.logging.log4j" % "log4j-core"       % Versions.log4j,
      "org.apache.logging.log4j" % "log4j-slf4j-impl" % Versions.log4j,
      "com.lmax" % "disruptor" % Versions.disruptor
    )

    val jackson = Seq(
      "com.fasterxml.jackson.core" % "jackson-databind" % Versions.jackson
    )

    val embeddedKafka = Seq(
      "io.github.embeddedkafka" %% "embedded-kafka" % Versions.kafka
    )

    val ziocats = Seq(
      "dev.zio" %% "zio-interop-cats" % Versions.zioInteropCats
    )

    val quillDB = Seq(
      "io.getquill" %% "quill-jdbc-zio" % Versions.ZioQuill,
      "org.postgresql" % "postgresql" % Versions.PostgresSql,
      "org.flywaydb" % "flyway-core" % Versions.Flyway
    )

    val http4s = Seq(
      "org.http4s" %% "http4s-blaze-server" % Versions.http4s,
      "org.http4s" %% "http4s-dsl" % Versions.http4s,
      "org.http4s" %% "http4s-circe" % Versions.http4s
    )

    val twillo = Seq("com.twilio.sdk" % "twilio" % "8.29.0")

    val mail = Seq("org.apache.commons" % "commons-email" % "1.5")

  }
}
