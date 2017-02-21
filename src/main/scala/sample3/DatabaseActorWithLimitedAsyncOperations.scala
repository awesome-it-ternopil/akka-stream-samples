package sample3

import akka.actor.Actor

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by viktor on 21.02.17.
  */
class DatabaseActorWithLimitedAsyncOperations  extends Actor {

  var messages: Seq[String] = Nil
  var count: Int = 0
  var outstanding: Int = 0

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

    case Decrement =>
      outstanding -= 1
      if (count >= 1000) {
        insert()
      }

    case other => println(other)
  }

  private def insert(): Unit = {
    if (count > 0 && outstanding < 10) {
      outstanding += 1
      val (insert, remaining) = messages.splitAt(1000)
      messages = remaining
      count = remaining.size
      Database.insertDataList(insert) andThen {
        case _ => self ! Decrement
      }
    }
  }

}

case object Decrement
