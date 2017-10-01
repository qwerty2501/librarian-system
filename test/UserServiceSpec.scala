import com.google.inject.Provider
import controllers.UserController
import dao.UserDAO
import forms.StartCreateUserRequestForm
import models.User
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.{Application, Play}
import play.api.test.Injecting
import services.{UserService, UserServiceImpl}
import utilities.ApplicationSetting

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent._

class UserServiceSpec extends PlaySpec {

  //テストケースが圧倒的に不足しているが、時間も圧倒的にないため・・・すみません
  "UserService post user create request" should{

    "mail token succeed" in {
      val userService = new UserServiceImpl(new {} with UserDAO{
        override def findIn(ids: Seq[Int]): Future[Seq[User]] = ???
        override def find(id: Int): Future[Seq[User]] = ???
        override def find(mail: String): Future[Seq[User]] = Future.apply(List[User]())
        override def find(mail: String, passwordHash: String): Future[Seq[User]] = ???
        override def insert(user: User): Future[Int] = ???


      },new{} with ApplicationSetting {
        override def userRegistrationRequestKey: String = "userRegistrationRequestKey"

        override def userPasswordHashKey: String = "userPasswordHashKey"
      })

      val mailTokenFuture = userService.userRegistrationRequest(StartCreateUserRequestForm("succeed@mail.com"))

      val mailTokenEither = Await.result(mailTokenFuture,Duration.Inf)

      mailTokenEither.isRight must be(true)

      mailTokenEither.right.get.token.length must not be 0
    }
  }
}
