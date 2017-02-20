package sample3

import akka.actor.Actor
import akka.actor.Actor.Receive

/**
  * Created by viktor on 20.02.17.
  */
class SimpleDatabaseActor extends Actor {

  override def receive: Receive = {
    case InsertData(message) => Database.insertData(message)

    case other => println(other)
  }
}

case class InsertData(message: String)
