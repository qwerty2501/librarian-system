package controllers

import javax.inject.{Inject, Singleton}

import play.api.mvc.MessagesActionBuilder
import utilities.AkkaDispatcherProvider


@Singleton
class HomeController @Inject()(akkaDispatcherProvider: AkkaDispatcherProvider,messagesAction: MessagesActionBuilder) extends BaseController(akkaDispatcherProvider,messagesAction) with Secured {
  def getIndex = withAuth{userID => implicit request =>
    Redirect(routes.StatusController.getStatuses()).withSession(("userID",userID.toString()))
  }
}
