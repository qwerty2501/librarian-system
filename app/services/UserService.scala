package services

import java.sql.Timestamp
import java.time.LocalDateTime
import java.util.{Base64, UUID}
import javax.inject._

import com.google.inject.ImplementedBy
import controllers.routes
import dao._
import forms.StartCreateUserRequestForm
import models._
import play.api.libs.mailer.{Email, MailerClient}
import utilities.{AkkaDispatcherProvider, ApplicationError}

import scala.concurrent.{Await, ExecutionContext, Future}
import ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import pdi.jwt.{JwtAlgorithm, JwtJson}
import play.{Application, Play}
import play.api.libs.json.Json


@ImplementedBy(classOf[UserServiceImpl])
trait UserService {
  def userRegistrationRequest(mail:String):Future[Either[ApplicationError,String]]
}

class UserServiceImpl @Inject()(val userDAO:UserDAO, akkaDispatcherProvider:AkkaDispatcherProvider,application :Provider[Application]) extends UserService{

  def userRegistrationRequest(mail :String):Future[Either[ApplicationError,String]]={
    userDAO.find(mail).map{users =>
      if (users.length > 0) {
        Left(ApplicationError("既に登録されているメールアドレスです"))
      }
      else{
        implicit val formatter = Json.format[UserRegistrationRequest]
        val claim = Json.toJsObject(UserRegistrationRequest(mail,LocalDateTime.now.plusDays(1)))
        val key = application.get.config().getString("userRegistrationRequestKey")
        Right(JwtJson.encode(claim,key,JwtAlgorithm.HS256))
      }
    }.recover{
      case e: Exception => Left(ApplicationError("データ処理に失敗しました",e))
    }
  }

  def t (e:Throwable): Unit ={

  }

}
