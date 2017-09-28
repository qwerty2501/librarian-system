package services

import java.sql.Timestamp
import java.time.LocalDateTime
import java.util.{Base64, UUID}
import javax.inject._

import akka.actor.Status.Success
import com.google.inject.ImplementedBy
import controllers.routes
import dao._
import forms.StartCreateUserRequestForm
import models._
import models.UserRegistrationRequest
import play.api.libs.mailer.{Email, MailerClient}
import utilities.{AkkaDispatcherProvider, ApplicationError, JWTHelpers}

import scala.concurrent.{Await, ExecutionContext, Future}
import ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import pdi.jwt.{JwtAlgorithm, JwtJson}
import play.{Application, Play}
import play.api.libs.json.Json


@ImplementedBy(classOf[UserServiceImpl])
trait UserService {
  def userRegistrationRequest(mail:String)(implicit executor: ExecutionContext):Future[Either[ApplicationError,String]]
  def checkBeforeCreateUser(token:String)(implicit executor: ExecutionContext): Future[Either[ApplicationError,String]]
}

class UserServiceImpl @Inject()(val userDAO:UserDAO,application :Provider[Application]) extends UserService{

  def userRegistrationRequest(mail :String)(implicit executor: ExecutionContext):Future[Either[ApplicationError,String]]={
    userDAO.find(mail).map{users =>
      if (users.length > 0) {
        Left(ApplicationError("既に登録されているメールアドレスです"))
      }
      else{
        val key = application.get.config().getString("userRegistrationRequestKey")
        Right(JWTHelpers.toJWT(UserRegistrationRequest(mail,LocalDateTime.now.plusDays(1)),key))
      }
    }
      .recover{
      case e: Exception => Left(ApplicationError("データ処理に失敗しました",e))
    }
  }

  def checkBeforeCreateUser(token:String)(implicit executor: ExecutionContext): Future[Either[ApplicationError,String]] ={
    val key = application.get.config().getString("userRegistrationRequestKey")
    val userRegistrationRequest = JWTHelpers.fromJWT[UserRegistrationRequest](token,key).getOrElse(UserRegistrationRequest("",null))
    if(userRegistrationRequest.mail =="" || userRegistrationRequest.expiresAt == null || LocalDateTime.now.isAfter(userRegistrationRequest.expiresAt)){
        return Future.apply(Left(ApplicationError("登録確認メールの有効期限が切れているか、無効なトークンです")))
    }
    userDAO.find(userRegistrationRequest.mail).map{users=>
      if(users.length > 0){
        Left(ApplicationError("既に登録されているメールアドレスです"))
      }

      Right(JWTHelpers.toJWT(UserRegistrationRequest(userRegistrationRequest.mail,LocalDateTime.now.plusHours(3)),key))
    }

  }

}
