package sample3

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.TextMessage
import akka.io.Tcp.Message
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._

import scala.io.StdIn

/**
  * Created by viktor on 20.02.17.
  */
object Sample3App extends App {

  implicit val system = ActorSystem("sample3")
  implicit val materializer = ActorMaterializer()

  val database = system.actorOf(Props[SimpleDatabaseActor], "simple-database-actor")

  val webSocketService = Flow[Message].collect {
    case TextMessage.Strict(text) =>
      database ! InsertData(text)
      TextMessage(text)
  }

  val route = path("sample3") {
    get {
      handleWebSocketMessages(webSocketService)
    }
  }


  val binding = Http().bindAndHandle(route, "localhost", 8080)

  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
  StdIn.readLine()
  binding
    .flatMap(_.unbind())
    .onComplete(_ => system.terminate())
}
