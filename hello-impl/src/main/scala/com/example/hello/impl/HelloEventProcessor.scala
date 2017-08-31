package com.example.hello.impl

import akka.persistence.query.Offset
import akka.{NotUsed, Done}
import akka.stream.scaladsl.Flow
import com.example.hello.api.Hello
import com.example.hello.impl.repositories.HelloRepo
import com.lightbend.lagom.scaladsl.persistence.{AggregateEventTag, EventStreamElement, ReadSideProcessor}
import com.lightbend.lagom.scaladsl.persistence.ReadSideProcessor.ReadSideHandler

import scala.concurrent.{Future, ExecutionContext}

/**
  * Created by knoldus on 31/8/17.
  */
class HelloEventProcessor(userRepository: HelloRepo)(implicit ec: ExecutionContext)
  extends ReadSideProcessor[HelloEvent] {

  override def buildHandler(): ReadSideProcessor.ReadSideHandler[HelloEvent] = {
    new ReadSideHandler[HelloEvent] {
      override def handle(): Flow[EventStreamElement[HelloEvent], Done, NotUsed] = {
        Flow[EventStreamElement[HelloEvent]].mapAsync(4) { eventElement => {
          handleEvent(eventElement.event, eventElement.offset)
        }}
      }
    }
  }

  private def handleEvent(eventStreamElement: HelloEvent, offset: Offset): Future[Done] = {
    eventStreamElement match {
      case msgChanged: GreetingMessageChanged => userRepository.addNewMessage(Hello(msgChanged.message.id, msgChanged.message.message))
    }
  }

  override def aggregateTags: Set[AggregateEventTag[HelloEvent]] = Set(HelloEvent.Tag)
  
}
