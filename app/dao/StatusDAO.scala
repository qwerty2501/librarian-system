package dao

import java.time.LocalDateTime
import javax.inject.Inject

import com.google.inject.ImplementedBy
import models.{Status}
import play.api.db.slick.DatabaseConfigProvider


import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[StatusDAOImpl])
trait StatusDAO {
  def insert(status:Status):Future[Int]
}

class StatusDAOImpl @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends BaseDAO with StatusDAO{
  import profile.api._
  private val statuses = TableQuery[StatusTable]
  override def insert(status: Status): Future[Int] = db.run(statuses += status)

  private class StatusTable(tag: Tag) extends Table[Status](tag, "STATUSES") {


    def id = column[Int]("ID", O.PrimaryKey,O.AutoInc)
    def userID = column[Int]("ID")
    def text = column[String]("TEXT")
    def createdAt = column[LocalDateTime]("CREATED_AT")
    def updatedAt = column[LocalDateTime]("UPDATED_AT")

    def * = (id,userID,text,createdAt,updatedAt) <> ((Status.apply _).tupled , Status.unapply _)
  }
}
