package sample3

import scala.collection.mutable

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

  def insertData(data: String): Long = {
    val id = nextId
    database += DatabaseItem(id, data)
    id
  }

  def insertDataList(data: List[String]): List[Long] = {
    val items = data.map(DatabaseItem(nextId, _))
    database ++= items
    items.map(_.id)
  }

}

case class DatabaseItem(id: Long, data: String)