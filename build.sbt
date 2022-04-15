import Dependencies.Libraries._

addCommandAlias("fmt", "all scalafmtSbt scalafmtAll")

name := "notification-engine"
version := "1.0"
scalaVersion := "2.13.4"



lazy val common = project
  .in(file("modules/common"))
  .settings(
    cancelable := false,
    libraryDependencies ++=
      log4j ++ embeddedKafka ++ zioKafka
        ++ zio ++ zioLogging ++ log4j ++ zioConfig ++ circe ++ jackson
        ++ ziocats
  )

lazy val notification = project
  .in(file("modules/notification-engine"))
  .settings(
    cancelable := false,
    libraryDependencies ++= twillo ++ mail
  ).dependsOn(common)


lazy val gateway = project
  .in(file("modules/notification-gateway"))
  .dependsOn(common)


