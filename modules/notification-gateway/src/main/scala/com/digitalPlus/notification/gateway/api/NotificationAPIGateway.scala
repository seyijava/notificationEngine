package com.digitalPlus.notification.gateway.api

import com.digitalPlus.notification.common.messages.Error
import com.digitalPlus.notification.common.messages.model.{EmailMessage, SMSMessage, WebHookMessage, emailMessageDecoder, smsMessageDecoder, webHookMessageDecoder}
import com.digitalPlus.notification.gateway.service.NotificationService
import zhttp.http.Method.{GET, POST}
import zhttp.http.{Http, HttpApp, Response, _}
import zio.json._
import zio.{RIO, URIO, ZIO}
object NotificationAPIGateway {

   val RootPath = "notification"

   val apiRoute: HttpApp[NotificationService, Throwable] =  Http.collectZIO {

    case GET -> _ /  RootPath / "health" => ZIO.succeed(Response.ok)

    case req@POST -> _ / RootPath/ "sms" =>
      httpResponseHandler[NotificationService, Unit](
        for {
          body <- req.bodyAsString
          smsMessage <- ZIO.fromEither(body.fromJson[SMSMessage].left.map(e => Error.DecodeError(e)))
          _ <- NotificationService(_.sendSMSEvent(smsMessage))
        } yield (), _ => Response.ok
      )

    case req@POST -> _ /RootPath/  "email" =>
      httpResponseHandler[NotificationService, Unit](
        for {
          body <- req.bodyAsString
          emailEvent <- ZIO.fromEither(body.fromJson[EmailMessage].left.map(e => Error.DecodeError(e)))
          _ <- NotificationService(_.sendEmailEvent(emailEvent))
        } yield (), _ => Response.ok
      )

    case req@POST -> _ / RootPath/ "webhook" =>
      httpResponseHandler[NotificationService, Unit](
        for {
          body <- req.bodyAsString
          webHookEvent <- ZIO.fromEither(body.fromJson[WebHookMessage].left.map(e => Error.DecodeError(e)))
          _ <- NotificationService(_.sendWebHookEvent(webHookEvent))
        } yield (), _ => Response.ok
      )
  }


  def httpResponseHandler[R, A](rio: RIO[R, A], onSuccess: A => Response): URIO[R, Response] =
    rio.fold(
      {
        case Error.DecodeError(msg) => Response.fromHttpError(HttpError.BadRequest(msg))
        case Error.NotFound(msg)    => Response.fromHttpError(HttpError.UnprocessableEntity(msg))
        case e                      => Response.fromHttpError(HttpError.InternalServerError(e.getMessage))
      },
      onSuccess
    )
}
