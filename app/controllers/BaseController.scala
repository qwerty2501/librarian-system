package controllers

import javax.inject.Inject

import play.api.mvc.{Action, _}
import utilities.AkkaDispatcherProvider

abstract class BaseController @Inject()(akkaDispatcherProvider:AkkaDispatcherProvider,ma: MessagesActionBuilder) extends InjectedController with Secured {
  implicit val nonBlockingDispatcher = akkaDispatcherProvider.nonBlockingDispatcher
  override val messagesAction = ma
}
