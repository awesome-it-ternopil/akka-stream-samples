package sample3

import akka.actor.{Actor, ActorRef}

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by viktor on 21.02.17.
  */
class DatabaseActorWithScheduler extends Actor {

  var messages: Seq[String] = Nil
  var count: Int = 0

  override def preStart(): Unit = {
    context.system.scheduler.scheduleOnce(1.second) {
      self ! Insert
    }
  }

  override def receive: Receive = {
    case InsertData(message) =>
      messages = message +: messages
      count += 1
      if (count == 1000) {
        insert()
      }

    case Insert =>
      insert()
      context.system.scheduler.scheduleOnce(1.second) {
        self ! Insert
      }

    case other => println(other)
  }

  private def insert(): Unit = {
    if (count > 0) {
      Database.insertDataList(messages)
      messages = Nil
      count = 0
    }
  }

}

case object Insert