package sample2

import java.nio.file.Paths

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{FileIO, Flow, Framing, Keep, Sink, Source}
import akka.util.ByteString

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

/**
  * Created by viktor on 08.03.17.
  */
object AkkaStreamSample extends App {

  implicit val system = ActorSystem("QuickStart")
  implicit val materializer = ActorMaterializer()

  val filePath = Paths.get("src/main/scala/sample2/forTest.txt")

  val source = FileIO.fromPath(filePath)

  val parseFileFlow = Framing.delimiter(ByteString(System.lineSeparator()), maximumFrameLength = 1024, allowTruncation = true).map(_.utf8String)

  val filterTagsFlow = Flow[String].mapAsync(10)(asyncParse)

  val flatMapFlow = Flow[Array[String]].flatMapConcat(a => Source.fromIterator(() => a.toIterator))

  val counterFlow = Flow[String].mapAsync(1)(tag => checkExistAsync(tag).map(if (_) 1 else 0)).fold(0)(_ + _)

  val sink = Sink.head[Int]

  def asyncParse(line: String): Future[Array[String]] = {
    Future {
      val rs = line.split(" ").filter(_.startsWith("#"))
      println(rs.mkString(" | "))
      rs
    }
  }

  def checkExistAsync(tag: String): Future[Boolean] = {
    Future {
      //request to db for check
      true
    }
  }

  val result = source.via(parseFileFlow).via(filterTagsFlow).via(flatMapFlow).via(counterFlow).toMat(sink)(Keep.right).run()

  result.onComplete {
    case Success(count) =>
      println(Console.GREEN + s"count = $count")
      system.terminate()
    case Failure(e) =>
      println(Console.RED + "Error: " + e.getMessage)
      system.terminate()
  }

}
