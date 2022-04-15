package com.digitalPlus.notification.engine.infrastructure.webhook

import com.digitalPlus.notification.common.messages.model.WebHookMessage
import zhttp.http.Method.POST
import zhttp.http.{Headers, HttpData, Request, Response, Status, URL}
import zio.json.JsonDecoder
import zio.{Accessible, Clock, Function0ToLayerOps, Function1ToLayerOps, Task, UIO, URLayer, ZIO}

trait WebHookConnector {

  def sendWebHookNotification(webHookMessage: WebHookMessage)(
      f: WebHookMessage => Request
  ): UIO[Either[String, Unit]]
}

case class WebHookConnectorLive(httpClientAdapter: HttpClientAdapter)
    extends WebHookConnector {
  override def sendWebHookNotification(
      webHookMessage: WebHookMessage
  )(f: WebHookMessage => Request): UIO[Either[String, Unit]] = {
    ZIO.log("Sending WebHook Notification") *> handleResponse(
      httpClientAdapter
        .executeRequest(f(webHookMessage))
        .provideLayer(Clock.live)
    )
  }

  def handleResponse(response: Task[Response]): UIO[Either[String, Unit]] = {
    response.fold(
      failure => Left(failure.getMessage),
      success =>
        success.status match {
          case Status.Ok => Right()
          case Status.BadRequest =>
            Left(s"Server Error ${Status.BadRequest}.toString")
          case Status.NotFound =>
            Left(s"Server Error ${Status.NotFound}.toString")
          case Status.InternalServerError =>
            Left(s"Server Error $Status.InternalServerError.toString")
          case _ => Left("Server Error" + Status.InternalServerError.toString)
        }
    )
  }

}

object WebHookConnector extends Accessible[WebHookConnector] {
  val live: URLayer[HttpClientAdapter, WebHookConnectorLive] =
    WebHookConnectorLive.toLayer
   val contentType = "application/json"
  val webHookRequest: WebHookMessage => Request = webhookMsg => {
    val uri = URL.empty.setHost(webhookMsg.host).setPath(webhookMsg.hookUrl).setPort(webhookMsg.port)
    Request(method = POST,data = HttpData.fromString(webhookMsg.encodedPayload),url = uri, headers =  Headers.contentType(contentType))
  }
}
