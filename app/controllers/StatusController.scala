package controllers

import javax.inject.{Inject, Singleton}

import forms.{NewStatusForm, Validations}
import models.StatusWithUser
import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.mvc.{AnyContent, MessagesActionBuilder, MessagesRequest, Result}
import services.StatusService
import utilities.AkkaDispatcherProvider

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

@Singleton
class StatusController @Inject()(statusService: StatusService, akkaDispatcherProvider:AkkaDispatcherProvider,ma: MessagesActionBuilder) extends BaseController(akkaDispatcherProvider,ma) with Secured{
  private val newStatusForm = Form(
    mapping(
      "text" -> Validations.statusTextValidation
    )(NewStatusForm.apply)(NewStatusForm.unapply)
  )
  def getStatuses = withAuthAsync{userID => implicit request=>
    getStatusesInternal(userID)(request)(newStatusForm)
  }

  private def getStatusesInternal ={userID:Int => implicit request:MessagesRequest[AnyContent]=> form:Form[NewStatusForm]=>
    statusService.getAllStatuses(userID).map{
      case Left(error) => BadRequest(views.html.statuses(List[StatusWithUser](),newStatusForm,error.message))
      case Right(statuses)=>Ok(views.html.statuses(statuses,form))
    }
  }

  def postStatus = withAuthAsync {userID => implicit request=>
    val bindedNewStatusForm = newStatusForm.bindFromRequest()
    bindedNewStatusForm.fold(
      formWithErrors => getStatusesInternal(userID)(request)(formWithErrors),
      succeedNewStatusForm => statusService.createStatus(userID,succeedNewStatusForm).flatMap{
        case Left(error)=> getStatusesInternal(userID)(request)(bindedNewStatusForm.withGlobalError(error.message))
        case Right(_)=> Future.apply(Redirect(routes.StatusController.getStatuses()))
      }
    )
  }
}
