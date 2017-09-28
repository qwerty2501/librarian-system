package controllers

import java.util.UUID
import javax.inject._

import forms._
import play.api._
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._
import services.{MailService, UserService}
import utilities.{AkkaDispatcherProvider, ApplicationError}
import play.api.i18n.Messages.Implicits._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.mailer.{Email, MailerClient}

import scala.concurrent.{ExecutionContext, Future}
import ExecutionContext.Implicits.global

@Singleton
class UserController @Inject()(service:UserService,akkaDispatcherProvider:AkkaDispatcherProvider,mailService: MailService,messagesAction: MessagesActionBuilder) extends InjectedController{

  private val startCreateUserRequestForm = Form(
    mapping(
      "mail"-> email
    )(StartCreateUserRequestForm.apply)(StartCreateUserRequestForm.unapply)
  )

  def create(requestKey:String) = messagesAction.async {implicit request: MessagesRequest[AnyContent]=>
    implicit val nonBlockingDispatcher = akkaDispatcherProvider.nonBlockingDispatcher
    service.checkBeforeCreateUser(requestKey).map{
      case Right(_) => Ok(views.html.startCreateUser(startCreateUserRequestForm))
      case Left(error)=> BadRequest("BadRequest")
    }

  }

  def createRequest = messagesAction {implicit request: MessagesRequest[AnyContent]=>
    Ok(views.html.startCreateUser(startCreateUserRequestForm))
  }

  def createRequestResult = messagesAction.async { implicit request =>

    val bindedFormRequest = startCreateUserRequestForm.bindFromRequest
    implicit val nonBlockingDispatcher = akkaDispatcherProvider.nonBlockingDispatcher
    bindedFormRequest.fold(
      formWithErrors =>Future.apply(BadRequest(views.html.startCreateUser(formWithErrors))),

      createUserRequest =>service.userRegistrationRequest(createUserRequest.mail)
        .map{
          case Right(requestKey) => {
            sendUserRegistrationNoticeMail(createUserRequest.mail,requestKey,request)
            Ok(views.html.startCreateUserResult("ユーザ作成リクエスト","招待メールを送信しました"))
          }
          case Left(error)  => BadRequest(views.html.startCreateUser(bindedFormRequest.withGlobalError(error.message,null)))
        }
    )
  }
  private def sendUserRegistrationNoticeMail(mailTo:String, requestKey:String,request:Request[_]): Unit ={
    //メール送信自体はModelの機能だが、ユーザからするとメールはViewの機能であり、メール文の生成はコントローラで行う
    val url = routes.UserController.create(requestKey).absoluteURL()(request)
    val body = views.html.userRegistrationMail(url).body

    //メール送信完了まで待機してしまうと、レスポンスが非常に遅くなってしまうため、待機しない
    mailService.sendMail(mailTo,"ユーザ登録通知", body)
  }


}
