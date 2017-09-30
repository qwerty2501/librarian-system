package controllers

import javax.inject.{Inject, Singleton}

import play.api.mvc.MessagesActionBuilder
import utilities.AkkaDispatcherProvider


@Singleton
class HomeController @Inject()(akkaDispatcherProvider: AkkaDispatcherProvider,messagesAction: MessagesActionBuilder) extends BaseController(akkaDispatcherProvider) {
  def getIndex = messagesAction{
    Ok(views.html.index("ホーム"))
  }
}
