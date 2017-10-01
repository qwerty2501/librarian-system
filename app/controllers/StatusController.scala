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
    getStatusesInternal(userID)(request)(newStatusForm)(null)
  }

  private def getStatusesInternal ={userID:Int => implicit request:MessagesRequest[AnyContent]=> form:Form[NewStatusForm]=> errorMessage:String =>
    statusService.getAllStatuses(userID).map{
      case Left(error) => BadRequest(views.html.statuses(userID,List[StatusWithUser](),newStatusForm,error.message))
      case Right(statuses)=>Ok(views.html.statuses(userID, statuses,form,errorMessage)).withSession("userID"->userID.toString())
    }
  }

  def postStatus = withAuthAsync {userID => implicit request=>
    val bindedNewStatusForm = newStatusForm.bindFromRequest()
    bindedNewStatusForm.fold(
      formWithErrors => getStatusesInternal(userID)(request)(formWithErrors)(null),
      succeedNewStatusForm => statusService.createStatus(userID,succeedNewStatusForm).flatMap{
        case Left(error)=> getStatusesInternal(userID)(request)(bindedNewStatusForm.withGlobalError(error.message))(null)
        case Right(_)=> Future.apply(Redirect(routes.StatusController.getStatuses()))
      }
    )
  }

  def deleteStatus(argStatusID:String) = withAuthAsync{userID=> implicit request=>
    val statusID = argStatusID.toInt
    statusService.deleteStatus(userID,statusID).flatMap{
      case Left(error)=>getStatusesInternal(userID)(request)(newStatusForm)(error.message)
      case Right(_) => Future.apply(Redirect(routes.StatusController.getStatuses()))
    }
  }

}
