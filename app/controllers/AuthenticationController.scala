package controllers

import javax.inject.{Inject, Singleton}

import forms.{LoginForm, StartCreateUserRequestForm, Validations}
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.Forms.mapping
import play.api.mvc.MessagesActionBuilder
import utilities.AkkaDispatcherProvider


@Singleton
class AuthenticationController @Inject() (akkaDispatcherProvider:AkkaDispatcherProvider,messagesAction: MessagesActionBuilder) extends BaseController(akkaDispatcherProvider) {
  private val loginForm = Form(
    mapping(
      "mail"-> email,
      "password" -> Validations.passwordValication
    )(LoginForm.apply)(LoginForm.unapply)
  )

  def getLogin= messagesAction{implicit request =>
    Ok(views.html.login(loginForm))
  }
}
