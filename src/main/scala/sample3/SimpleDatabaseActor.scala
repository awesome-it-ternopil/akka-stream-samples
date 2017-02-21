package sample3

import akka.actor.Actor
import akka.actor.Actor.Receive
import scala.concurrent.ExecutionContext.Implicits.global
/**
  * Created by viktor on 20.02.17.
  */
class SimpleDatabaseActor extends Actor {

  override def receive: Receive = {
    case InsertData(message) =>
      val s = sender()
      Database.insertData(message).map(s ! _.toString)

    case other => println(other)
  }
}

