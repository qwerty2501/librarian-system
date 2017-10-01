package controllers

import javax.inject.{Inject, Singleton}

import forms.{NewStatusForm, Validations}
import models.StatusWithUser
import play.api.data.Form
import play.api.data.Forms.mapping
import play.api.mvc.MessagesActionBuilder
import services.StatusService
import utilities.AkkaDispatcherProvider

@Singleton
class StatusController @Inject()(statusService: StatusService, akkaDispatcherProvider:AkkaDispatcherProvider,ma: MessagesActionBuilder) extends BaseController(akkaDispatcherProvider,ma) with Secured{
  private val newStatusForm = Form(
    mapping(
      "text" -> Validations.statusTextValidation
    )(NewStatusForm.apply)(NewStatusForm.unapply)
  )
  def getStatuses = withAuthAsync{userID => implicit request=>
    statusService.getAllStatuses(userID).map{
      case Left(error) => BadRequest(views.html.statuses(List[StatusWithUser](),newStatusForm,error.message))
      case Right(statuses)=>Ok(views.html.statuses(statuses,newStatusForm))
    }
  }

  def postStatuses = withAuth {userID => implicit request=>
    Ok("")
  }
}
