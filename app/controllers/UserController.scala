package controllers

import javax.inject._

import play.api._
import play.api.mvc._
import services.UserService
import utilities.AkkaDispatcherProvider

import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global

@Singleton
class UserController @Inject()(val service:UserService,akkaDispathcerProvider:AkkaDispatcherProvider) extends InjectedController {


  def index = Action.async {

    service.test().map{u=>
      Ok(views.html.index("Your new application is ready."))
    }(akkaDispathcerProvider.nonBlockingDispatcher)

  }

}
