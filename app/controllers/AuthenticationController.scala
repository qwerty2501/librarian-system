package controllers

import javax.inject.{Inject, Singleton}

import forms.{LoginForm, StartCreateUserRequestForm, Validations}
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.Forms.mapping
import play.api.mvc.MessagesActionBuilder
import services.AuthenticationService
import utilities.AkkaDispatcherProvider

import scala.concurrent.Future


@Singleton
class AuthenticationController @Inject() (akkaDispatcherProvider:AkkaDispatcherProvider,ma: MessagesActionBuilder,authenticationService:AuthenticationService) extends BaseController(akkaDispatcherProvider,ma) {
  private val loginForm = Form(
    mapping(
      "loginMail"-> email,
      "loginPassword" -> nonEmptyText //エラーでパスワード桁数がわかってしまうためあえてnonEmptyTextのみにする
    )(LoginForm.apply)(LoginForm.unapply)
  )

  def getLogin= messagesAction{implicit request =>
    Ok(views.html.login(loginForm))
  }

  def postLogin = messagesAction.async{implicit  request =>
    val bindedLoginForm = loginForm.bindFromRequest()
    bindedLoginForm.fold(
      formWithErrors=> Future.apply(BadRequest(views.html.login(formWithErrors))),
      succeedLoginForm => authenticationService.authenticate(succeedLoginForm.mail,succeedLoginForm.password).map{
        case Left(error)=> BadRequest(views.html.login(bindedLoginForm.withGlobalError(error.message)))
        case Right(user) => Redirect(routes.HomeController.getIndex()).withSession(("userID" ,user.id.toString))
      }
    )
  }
}
