package com.digitalPlus.notification.engine.infrastructure.webhook

import zhttp.http.{Request, Response}
import zhttp.service.Client.Config
import zhttp.service.{ChannelFactory, Client, EventLoopGroup}
import zio.{
  Accessible,
  Clock,
  Function0ToLayerOps,
  Schedule,
  URLayer,
  ZIO,
  durationInt
}

trait HttpClientAdapter {
  def executeRequest(request: Request): ZIO[Clock, Throwable, Response]
  def forEachRequest(
      requests: List[Request]
  ): ZIO[Clock, Throwable, List[Response]]
}

case class HttpClientAdapterLive() extends HttpClientAdapter {

  override def executeRequest(
      request: Request
  ): ZIO[Clock, Throwable, Response] = {
    Client
      .request(request, Config.empty)
      .retry(Schedule.recurs(2) && Schedule.exponential(10.milliseconds))
      .provide(ChannelFactory.auto ++ EventLoopGroup.auto(), Clock.live)
  }

  override def forEachRequest(
      requests: List[Request]
  ): ZIO[Clock, Throwable, List[Response]] =
    ZIO.foreach(requests) { r =>
      ZIO.log("Executing Request") *> executeRequest(r).delay(10.milliseconds)
    }
}

object HttpClientAdapter extends Accessible[HttpClientAdapter] {
  val live: URLayer[Any, HttpClientAdapterLive] = HttpClientAdapterLive.toLayer
}
