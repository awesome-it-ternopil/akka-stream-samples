package sample3

import akka.actor.Actor
/**
  * Created by viktor on 21.02.17.
  */
class DatabaseActorWithBuffer extends Actor {

  var messages: Seq[String] = Nil
  var count: Int = 0

  override def receive: Receive = {
    case InsertData(message) =>
      messages = message +: messages
      count += 1
      if(count == 1000) {
        insert()
      }

    case other => println(other)
  }

  private def insert(): Unit = {
    Database.insertDataList(messages)
    messages = Nil
    count = 0
  }
}
