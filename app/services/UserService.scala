package services

import java.sql.Timestamp
import java.time.LocalDateTime
import java.util.UUID
import javax.inject._

import controllers.routes
import dao._
import forms.StartCreateUserRequestForm
import models._
import play.api.libs.mailer.{Email, MailerClient}
import utilities.{AkkaDispatcherProvider, ApplicationError}

import scala.concurrent.{Await, ExecutionContext, Future}
import ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

trait UserService {
  def userRegistrationRequest(requestForm:StartCreateUserRequestForm):Future[Either[ApplicationError,UUID]]
}

class UserServiceImpl @Inject()(val userDAO:UserDAO,userRegistrationRequest:UserRegistrationRequestDAO, akkaDispatcherProvider:AkkaDispatcherProvider) extends UserService{

  def userRegistrationRequest(requestForm:StartCreateUserRequestForm):Future[Either[ApplicationError,UUID]]={
    userDAO.find(requestForm.mail).map{users =>
      if (users.length > 0) {
        Left(new ApplicationError("既に登録されているメールアドレスです"))
      }
      else{
        val expiredAt = LocalDateTime.now().plusDays(1)
        val requestKey = UUID.nameUUIDFromBytes((expiredAt.toString() + requestForm.mail).getBytes())
        val insertFuture = userRegistrationRequest.insert( new UserRegistrationRequest(requestKey.toString(), requestForm.mail,expiredAt = Timestamp.valueOf(expiredAt),null,null))
        Await.result(insertFuture,Duration.Inf)
        Right(requestKey)
      }
    }.recover{
      case e: Exception => {
        t(e)
        Left(new ApplicationError("データ処理に失敗しました",e))
      }
    }
  }



  def t(e:Exception): Unit ={
    val ee = e
  }


  /*
  def createInviteURL(mail:String):String = {

  }
  */

}
