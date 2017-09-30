package services

import javax.inject.Inject

import com.google.inject.ImplementedBy
import dao.UserDAO
import models.User
import utilities.{ApplicationError, ApplicationSetting, HMACHelper}

import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[AuthenticationServiceImpl])
trait AuthenticationService {
  def authenticate(mail:String,password:String)(implicit executor: ExecutionContext): Future[Either[ApplicationError,User]]
}

class AuthenticationServiceImpl @Inject()(userDAO:UserDAO,applicationSetting :ApplicationSetting) extends AuthenticationService{

  def authenticate(mail:String,password:String)(implicit executor: ExecutionContext): Future[Either[ApplicationError,User]] ={
    val passwordHash = HMACHelper.generateHMAC(applicationSetting.userPasswordHashKey,password)
    userDAO.find(mail,passwordHash).map{users=>
      if (users.length == 0 ){
        Left(ApplicationError("ユーザ認証に失敗しました"))
      }

      else{
        Right(users.head)
      }
    }
  }
}