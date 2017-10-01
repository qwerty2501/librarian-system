package dao


import java.sql.Timestamp
import java.time.LocalDateTime
import javax.inject._

import com.google.inject.ImplementedBy

import scala.concurrent.{ExecutionContext, Future}
import models.User
import play.api.db.slick.DatabaseConfigProvider
import play.api.db.slick.HasDatabaseConfigProvider
import slick.jdbc.JdbcProfile

@ImplementedBy(classOf[UserDAOImpl])
trait UserDAO {
  def findIn(ids:Seq[Int]):Future[Seq[User]]
  def find(id:Int):Future[Seq[User]]
  def find(mail:String): Future[Seq[User]]
  def find(mail:String,passwordHash:String):Future[Seq[User]]
  def insert(user: User): Future[Int]
}

class UserDAOImpl @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends BaseDAO with UserDAO{
  import profile.api._
  private val users = TableQuery[UsersTable]

  override def findIn(ids: Seq[Int]): Future[Seq[User]] = db.run(users.filter(u=> u.id inSetBind ids).result)
  override def find(id: Int): Future[Seq[User]] = db.run(users.filter(u=>u.id === id).result)
  override def find(mail:String,passwordHash:String):Future[Seq[User]] = db.run(users.filter( u => (u.mail === mail) && (u.password === passwordHash)).result)
  override def find(mail:String): Future[Seq[User]] = db.run(users.filter(u=>u.mail === mail).result)

  override def insert(user: User): Future[Int] = db.run(users += user)

  class UsersTable(tag: Tag) extends Table[User](tag, "USERS") {


    def id = column[Int]("ID", O.PrimaryKey,O.AutoInc)
    def mail = column[String]("MAIL")
    def password = column[String]("PASSWORD")
    def name = column[String]("NAME")
    def createdAt = column[LocalDateTime]("CREATED_AT")
    def updatedAt = column[LocalDateTime]("UPDATED_AT")

    def * = (id,mail,password,name,createdAt,updatedAt) <> ((User.apply _).tupled , User.unapply _)
  }
}


