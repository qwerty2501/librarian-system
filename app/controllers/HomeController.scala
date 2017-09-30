package controllers

import javax.inject.{Inject, Singleton}

import play.api.mvc.MessagesActionBuilder
import utilities.AkkaDispatcherProvider


@Singleton
class HomeController @Inject()(akkaDispatcherProvider: AkkaDispatcherProvider,messagesAction: MessagesActionBuilder) extends BaseController(akkaDispatcherProvider,messagesAction) with Secured {
  def getIndex = withAuth{user => implicit request =>
    Ok(views.html.index())
  }
}
