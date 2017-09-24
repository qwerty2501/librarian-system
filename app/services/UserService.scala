package services

import javax.inject._

import dao.UserDAO
import models.User

import scala.concurrent.{ExecutionContext, Future}
import ExecutionContext.Implicits.global

trait UserService {
  def test(): Future[User]
}

class UserServiceImpl @Inject()(val dao:UserDAO) extends UserService{

  def test(): Future[User] = {

    val user = new User(0,"testmail","password","n",null,null,null)

    dao.insert(user) map{ _ =>
      user
    }
  }
}
