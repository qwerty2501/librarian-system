package dao


import java.sql.Timestamp
import java.time.LocalDateTime
import javax.inject._

import scala.concurrent.{ExecutionContext, Future}
import models.User
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile

trait UserDAO {
  def find(mail:String): Future[Seq[User]]

  def insert(user: User): Future[Unit]
}

class UserDAOImpl @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] with UserDAO{
  import profile.api._

  private val users = TableQuery[UsersTable]

  def find(mail:String): Future[Seq[User]] = db.run(users.filter(u=>u.mail === mail).result)

  def insert(user: User): Future[Unit] = db.run(users += user).map { _ => () }

  private class UsersTable(tag: Tag) extends Table[User](tag, "USERS") {


    def id = column[Int]("ID", O.PrimaryKey,O.AutoInc)
    def mail = column[String]("MAIL")
    def password = column[String]("PASSWORD")
    def name = column[String]("NAME")
    def createdAt = column[Timestamp]("CREATED_AT")
    def updatedAt = column[Timestamp]("UPDATED_AT")

    def * = (id,mail,password,name,createdAt,updatedAt) <> ((User.apply _).tupled , User.unapply _)
  }
}
