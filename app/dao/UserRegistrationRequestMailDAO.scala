package dao

import java.sql.Timestamp
import javax.inject.Inject

import models.UserRegistrationRequest
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile
import slick.lifted.Tag

import scala.concurrent.{ExecutionContext, Future}

trait UserRegistrationRequestDAO {
  def find(requestKey:String):Future[Seq[UserRegistrationRequest]]
  def insert(inviteUserMail:UserRegistrationRequest):Future[Unit]
}

class UserRegistrationRequestDAOImpl @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] with UserRegistrationRequestDAO{
  import profile.api._
  private val inviteUserMails = TableQuery[InviteUserMailsTable]
  def find(requestKey:String):Future[Seq[UserRegistrationRequest]] = db.run(inviteUserMails.filter(rur => rur.requestKey === requestKey).result)
  def insert(inviteUserMail:UserRegistrationRequest) = db.run(inviteUserMails +=  inviteUserMail).map(_=>())

  private class InviteUserMailsTable(tag: Tag) extends Table[UserRegistrationRequest](tag, "USER_REGISTRATION_REQUESTS") {


    def requestKey = column[String]("REQUEST_KEY", O.PrimaryKey)
    def mail = column[String]("MAIL")
    def expiredAt = column[Timestamp]("EXPIRED_AT")
    def createdAt = column[Timestamp]("CREATED_AT")
    def updatedAt = column[Timestamp]("UPDATED_AT")


    def * = (requestKey,mail,expiredAt,createdAt,updatedAt) <> ((UserRegistrationRequest.apply _).tupled , UserRegistrationRequest.unapply _)
  }
}
