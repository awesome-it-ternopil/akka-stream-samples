package sample0

import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, ThrottleMode}
import akka.stream.scaladsl.{Flow, Sink, Source}

import scala.concurrent.Future
import scala.concurrent.duration._

/**
  * Created by viktor on 20.02.17.
  */
object AkkaStreamSample extends App {

  implicit val system = ActorSystem("QuickStart")
  implicit val materializer = ActorMaterializer()

  val source: Source[Int, NotUsed] = Source(1 to 100)

  val sink: Sink[Any, Future[Done]] = Sink.foreach(println)

  val factorials = Flow[Int].scan(BigInt(1))((acc, next) => acc * next)

  val flow = source
    .via(factorials)
    .zipWith(source)((num, idx) => s"${idx - 1}! = $num")
    .throttle(1, 1.second, 1, ThrottleMode.shaping)
    .to(sink)

  flow.run()
}
