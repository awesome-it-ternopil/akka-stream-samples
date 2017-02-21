package sample3

import scala.collection.mutable
import scala.concurrent.Future

/**
  * Created by viktor on 20.02.17.
  */
object Database {

  var id = 0L

  private def nextId: Long = {
    id+=1
    id
  }

  private val database: mutable.Buffer[DatabaseItem] = mutable.Buffer.empty[DatabaseItem]

  def insertData(data: String): Future[Long] = {
    val id = nextId
    database += DatabaseItem(id, data)
    Future.successful(id)
  }

  def insertDataList(data: Seq[String]): Future[Seq[Long]] = {
    val items = data.map(DatabaseItem(nextId, _))
    database ++= items
    Future.successful(items.map(_.id))
  }

}

case class DatabaseItem(id: Long, data: String)