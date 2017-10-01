package controllers

import javax.inject.Inject

import play.api.mvc.{Action, _}
import utilities.AkkaDispatcherProvider

abstract class BaseController (akkaDispatcherProvider:AkkaDispatcherProvider,ma: MessagesActionBuilder) extends InjectedController with Secured {
  implicit val nonBlockingDispatcher = akkaDispatcherProvider.nonBlockingDispatcher
  def blockingDispatcher = akkaDispatcherProvider.blockingDispatcher
  override val messagesAction = ma
}
