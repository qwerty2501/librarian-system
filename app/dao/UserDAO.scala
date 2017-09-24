package dao


import java.sql.Timestamp
import java.time.LocalDateTime
import javax.inject._

import scala.concurrent.{ExecutionContext, Future}
import models.User
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile


class UserDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile]{
  import profile.api._

  private val users = TableQuery[UsersTable]

  def all(): Future[Seq[User]] = db.run(users.result)

  def insert(user: User): Future[Unit] = db.run(users += user).map { _ => () }

  private class UsersTable(tag: Tag) extends Table[User](tag, "USERS") {


    def id = column[Long]("ID", O.PrimaryKey,O.AutoInc)
    def mail = column[String]("MAIL")
    def password = column[String]("PASSWORD")
    def name = column[String]("NAME")
    def createdAt = column[Timestamp]("CREATED_AT")
    def updatedAt = column[Timestamp]("UPDATED_AT")
    def deletedAt = column[Timestamp]("DELETED_AT")

    def * = (id,mail,password,name,createdAt,updatedAt,deletedAt) <> ((User.apply _).tupled , User.unapply _)
  }
}
