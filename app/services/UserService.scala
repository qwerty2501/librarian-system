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
import utilities._

import scala.concurrent.{Await, ExecutionContext, Future}
import ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import pdi.jwt.{JwtAlgorithm, JwtJson}
import play.{Application, Play}
import play.api.libs.json.Json


@ImplementedBy(classOf[UserServiceImpl])
trait UserService {
  def existUser(id:Int)(implicit executor: ExecutionContext):Future[Either[ApplicationError,Boolean]]
  def userRegistrationRequest(form:StartCreateUserRequestForm)(implicit executor: ExecutionContext):Future[Either[ApplicationError,CreateUserRequestMailToken]]
  def progressCreateUserFromMailToken(mailToken:CreateUserRequestMailToken)(implicit executor: ExecutionContext): Future[Either[ApplicationError,CreateUserToken]]
  def createUserFromCreateToken(createToken:CreateUserToken, userCreateForm:CreateUserForm)(implicit executor:ExecutionContext):Future[Either[ApplicationError,_]]
}

class UserServiceImpl @Inject()(userDAO:UserDAO,applicationSetting :ApplicationSetting) extends UserService{
  def existUser(id:Int)(implicit executor: ExecutionContext):Future[Either[ApplicationError,Boolean]] ={
    userDAO.find(id).map{users=>
      Right(!users.isEmpty)
    }.recover{
      case e:Exception => Left(ApplicationError("データ処理に失敗しました",e))
    }
  }
  def userRegistrationRequest(form :StartCreateUserRequestForm)(implicit executor: ExecutionContext):Future[Either[ApplicationError,CreateUserRequestMailToken]]={
    val mail = form.mail
    userDAO.find(mail).map{users =>
      if (users.length > 0) {
        Left(ApplicationError("このメールアドレスで既にユーザが登録されています"))
      }
      else{
        Right(CreateUserRequestMailToken(JWTHelpers.toJWT(UserRegistrationRequest(mail,LocalDateTime.now.plusDays(1)),applicationSetting.userRegistrationRequestKey)))
      }
    }.recover{
      case e: Exception => Left(ApplicationError("データ処理に失敗しました",e))
    }
  }

  def progressCreateUserFromMailToken(token:CreateUserRequestMailToken)(implicit executor: ExecutionContext): Future[Either[ApplicationError,CreateUserToken]] ={
    val userRegistrationRequest = JWTHelpers.fromJWT[UserRegistrationRequest](token.token,applicationSetting.userRegistrationRequestKey).getOrElse(UserRegistrationRequest("",null))
    if(userRegistrationRequest.mail =="" || userRegistrationRequest.expiresAt == null || LocalDateTime.now.isAfter(userRegistrationRequest.expiresAt)){
        return Future.apply(Left(ApplicationError("登録確認メールの有効期限が切れているか、無効なトークンです")))
    }
    userDAO.find(userRegistrationRequest.mail).map{users=>
      if(users.length > 0){
        Left(ApplicationError("このメールアドレスで既にユーザが登録されています"))
      }

      Right(CreateUserToken(userRegistrationRequest.mail))
    }
  }

  def createUserFromCreateToken(createToken:CreateUserToken,userCreateForm:CreateUserForm)(implicit executor:ExecutionContext):Future[Either[ApplicationError,_]]={
    if (createToken.mail.length == 0){
      Left(ApplicationError("無効なトークンです"))
    }
    userDAO.find(createToken.mail).flatMap{ users =>
      if (users.length > 0) {
        Future.apply(Left(ApplicationError("このメールアドレスで既にユーザが登録されています")))
      }
      val passwordHash = HMACHelper.generateHMAC(applicationSetting.userPasswordHashKey,userCreateForm.password)
      userDAO.insert(User(0,createToken.mail,passwordHash,userCreateForm.name,null,null)).map(_=>Right(Nil))
    }.recover{
      case e:Exception=> Left(ApplicationError("ユーザ登録に失敗しました",e))
    }
  }
}
