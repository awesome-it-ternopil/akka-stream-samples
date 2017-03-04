package sample1

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.scaladsl.{
  Balance,
  Broadcast,
  Flow,
  GraphDSL,
  Merge,
  RunnableGraph,
  Sink,
  Source
}
import akka.stream.{ActorMaterializer, ClosedShape, ThrottleMode}

import scala.concurrent.duration._

/**
  * Created by viktor on 04.03.17.
  */
object AkkaStreamSample extends App {

  implicit val system = ActorSystem("QuickStart")
  implicit val materializer = ActorMaterializer()

  val source: Source[Int, NotUsed] = Source(1 to 100)

  val factorials: Flow[Int, String, NotUsed] = Flow[Int]
    .scan(BigInt(1))((acc, next) => acc * next)
    .zipWith(source)((num, idx) => s"${idx - 1}! = $num")

  val greenFlow = Flow[String].map(rs => Console.GREEN + rs)

  val onePerSecond = Flow[String].throttle(1, 1.second, 0, ThrottleMode.Shaping)

  val redFlow = Flow[String].map(rs => Console.RED + rs)
//    .via(onePerSecond)

  val sink = Sink.foreach(println)

  val graph = RunnableGraph.fromGraph(GraphDSL.create() { implicit builder =>
    import GraphDSL.Implicits._

    val merge = builder.add(Merge[String](2))

    val bcast = builder.add(Broadcast[String](2))

    source.via(factorials) ~> bcast ~> greenFlow ~> merge ~> sink
                              bcast ~> redFlow   ~> merge

//    val balance = builder.add(Balance[String](2))
//
//    source.via(factorials) ~> balance ~> greenFlow ~> merge ~> sink
//                              balance ~> redFlow   ~> merge

    ClosedShape
  })

  graph.run()

}
