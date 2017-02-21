package sample3

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Flow
import akka.util.Timeout

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.io.StdIn

/**
  * Created by viktor on 20.02.17.
  */
object Sample3Server extends App {

  implicit val system = ActorSystem("sample3")
  implicit val materializer = ActorMaterializer()
  implicit val timeout: Timeout = 5.seconds

  val simpleDBActor = system.actorOf(Props[SimpleDatabaseActor], "simple-database-actor")
  val DBActorWithBuffer = system.actorOf(Props[DatabaseActorWithBuffer], "database-actor-with-buffer")
  val DBActorWithScheduler = system.actorOf(Props[DatabaseActorWithScheduler], "database-actor-with-scheduler")
  val DBActorWithLimitAsyncOp = system.actorOf(Props[DatabaseActorWithLimitedAsyncOperations], "database-actor-with-limit-async-op")

  val simpleActorFlow = Flow[Message].mapAsync(1) {
    case TextMessage.Strict(text) =>
      (simpleDBActor ? InsertData(text)).mapTo[String].map(TextMessage.apply)
  }

  val actorWithBufferFlow = Flow[Message].collect {
    case TextMessage.Strict(text) =>
      DBActorWithBuffer ! InsertData(text)
      TextMessage("")
  }

  val actorWithSchedulerFlow = Flow[Message].collect {
    case TextMessage.Strict(text) =>
      DBActorWithScheduler ! InsertData(text)
      TextMessage("")
  }

  val actorWithLimitAsyncOp = Flow[Message].collect {
    case TextMessage.Strict(text) =>
      DBActorWithLimitAsyncOp ! InsertData(text)
      TextMessage("")
  }

  val akkaStreamFlow = Flow[Message].collect {
    case TextMessage.Strict(text) =>
      Future.successful(text)
  }.mapAsync(1)(message => message)
    .groupedWithin(1000, 1.second)
    .mapAsync(10)(Database.insertDataList)
    .map(ids => TextMessage(ids.mkString(", ")))

  val route = path("simple") {
    get {
      handleWebSocketMessages(simpleActorFlow)
    }
  } ~
    path("buffer") {
      get {
        handleWebSocketMessages(actorWithBufferFlow)
      }
    } ~
    path("scheduler") {
      get {
        handleWebSocketMessages(actorWithSchedulerFlow)
      }
    } ~
    path("async-limit") {
      get {
        handleWebSocketMessages(actorWithLimitAsyncOp)
      }
    } ~
    path("stream") {
      get {
        handleWebSocketMessages(akkaStreamFlow)
      }
    }

  val binding = Http().bindAndHandle(route, "localhost", 8080)

  println(s"Server run at http://localhost:8080/\nPress RETURN to stop...")
  StdIn.readLine()
  binding
    .flatMap(_.unbind())
    .onComplete(_ => system.terminate())
}
