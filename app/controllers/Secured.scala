package controllers

import play.api.mvc._

import scala.concurrent.Future


trait Secured {
  val messagesAction :MessagesActionBuilder
  def userID(request: RequestHeader) = request.session.get("userID")

  def onUnauthorized(request: RequestHeader) = {
    Results.Ok(views.html.index()).withNewSession
  }

  def withAuth (f: => Int => MessagesRequest[AnyContent] => Result) = {
    Security.Authenticated(userID, onUnauthorized) { id =>
      messagesAction(request => f(id.toInt)(request))
    }
  }

  def withAuthAsync (f: => Int => MessagesRequest[AnyContent] => Future[Result]) = {
    Security.Authenticated(userID, onUnauthorized) { id =>
      messagesAction.async(request => f(id.toInt)(request))
    }
  }
}