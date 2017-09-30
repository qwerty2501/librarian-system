package services

import java.sql.Timestamp
import java.time.LocalDateTime
import java.util.{Base64, UUID}
import javax.inject._

import akka.actor.Status.Success
import com.google.inject.ImplementedBy
import controllers.routes
import dao._
import forms.{CreateUserForm, StartCreateUserRequestForm}
import models._
import models.UserRegistrationRequest
import play.api.libs.mailer.{Email, MailerClient}
import utilities.{AkkaDispatcherProvider, ApplicationError, HMACHelper, JWTHelpers}

import scala.concurrent.{Await, ExecutionContext, Future}
import ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import pdi.jwt.{JwtAlgorithm, JwtJson}
import play.{Application, Play}
import play.api.libs.json.Json


@ImplementedBy(classOf[UserServiceImpl])
trait UserService {
  def userRegistrationRequest(mail:String)(implicit executor: ExecutionContext):Future[Either[ApplicationError,CreateUserRequestMailToken]]
  def progressCreateUserFromMailToken(mailToken:CreateUserRequestMailToken)(implicit executor: ExecutionContext): Future[Either[ApplicationError,CreateUserToken]]
  def createUserFromCreateToken(createToken:CreateUserToken, userCreateForm:CreateUserForm)(implicit executor:ExecutionContext):Future[Either[ApplicationError,_]]
}

class UserServiceImpl @Inject()(val userDAO:UserDAO,application :Provider[Application]) extends UserService{
  private def userRegistrationRequestKey = application.get.config().getString("userRegistrationRequestKey")
  private def userPasswordHashKey = application.get().config().getString("userPasswordHashKey")
  def userRegistrationRequest(mail :String)(implicit executor: ExecutionContext):Future[Either[ApplicationError,CreateUserRequestMailToken]]={
    userDAO.find(mail).map{users =>
      if (users.length > 0) {
        Left(ApplicationError("このメールアドレスで既にユーザが登録されています"))
      }
      else{
        val key = application.get.config().getString("userRegistrationRequestKey")
        Right(CreateUserRequestMailToken(JWTHelpers.toJWT(UserRegistrationRequest(mail,LocalDateTime.now.plusDays(1)),key)))
      }
    }.recover{
      case e: Exception => Left(ApplicationError("データ処理に失敗しました",e))
    }
  }

  def progressCreateUserFromMailToken(token:CreateUserRequestMailToken)(implicit executor: ExecutionContext): Future[Either[ApplicationError,CreateUserToken]] ={
    val userRegistrationRequest = JWTHelpers.fromJWT[UserRegistrationRequest](token.token,userRegistrationRequestKey).getOrElse(UserRegistrationRequest("",null))
    if(userRegistrationRequest.mail =="" || userRegistrationRequest.expiresAt == null || LocalDateTime.now.isAfter(userRegistrationRequest.expiresAt)){
        return Future.apply(Left(ApplicationError("登録確認メールの有効期限が切れているか、無効なトークンです")))
    }
    userDAO.find(userRegistrationRequest.mail).map{users=>
      if(users.length > 0){
        Left(ApplicationError("このメールアドレスで既にユーザが登録されています"))
      }

      Right(CreateUserToken(JWTHelpers.toJWT(UserRegistrationRequest(userRegistrationRequest.mail,LocalDateTime.now.plusHours(3)),userRegistrationRequestKey)))
    }
  }

  def createUserFromCreateToken(createToken:CreateUserToken,userCreateForm:CreateUserForm)(implicit executor:ExecutionContext):Future[Either[ApplicationError,_]]={
    val userRegistrationRequest = JWTHelpers.fromJWT[UserRegistrationRequest](createToken.token,userRegistrationRequestKey).getOrElse(UserRegistrationRequest("",null))
    if(userRegistrationRequest.mail =="" || userRegistrationRequest.expiresAt == null || LocalDateTime.now.isAfter(userRegistrationRequest.expiresAt)){
      return Future.apply(Left(ApplicationError("登録に必要な認証情報が無効です")))
    }
    userDAO.find(userRegistrationRequest.mail).map { users =>
      if (users.length > 0) {
        Left(ApplicationError("このメールアドレスで既にユーザが登録されています"))
      }
      val passwordHash = HMACHelper.generateHMAC(userPasswordHashKey,userCreateForm.password)
      val insertFuture = userDAO.insert(User(0,userRegistrationRequest.mail,passwordHash,userCreateForm.name,null,null))
      Await.result(insertFuture,Duration.Inf)
      Right(Nil)
    }.recover{
      case e:Exception=>{
        t(e)
        Left(ApplicationError("ユーザ登録に失敗しました",e))
      }
    }
  }

  def t(e:Exception): Unit ={
    val t = e.getMessage
    t.startsWith(t)
  }

}
