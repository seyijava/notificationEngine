package com.digitalPlus.notification.engine.infrastructure.kafka

import com.digitalPlus.notification.common.messages.Event
import zio.{Task, ZIO}



trait EventHandler {
  def handleEvent(event: Event): Task[Either[String, Unit]]
}
