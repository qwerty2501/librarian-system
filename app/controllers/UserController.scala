package controllers

import java.util.UUID
import javax.inject._

import akka.actor.Status.Success
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

  private val createUserForm = Form(
    mapping(
      "name" -> nonEmptyText(3,32),
      "password" -> nonEmptyText(8,16),
      "passwordConfirm"-> nonEmptyText(8,16)
    )(CreateUserForm.apply)(CreateUserForm.unapply)
      .verifying("パスワードが一致しません", field => field.password == field.passwordConfirm)
  )

  def postCreate() = messagesAction.async{implicit request =>
    val createToken = request.session.get("create_token").getOrElse("")
    implicit val nonBlockingDispatcher = akkaDispatcherProvider.nonBlockingDispatcher
    val bindedFormRequest = createUserForm.bindFromRequest
    bindedFormRequest.fold(
      formWithErrors => Future.apply(BadRequest(views.html.createUser(formWithErrors))),
      createUser =>service.createUserFromCreateToken(createToken,createUser).map{
        case Right(_)=>  Ok(views.html.createUserResult())
        case Left(error)=>BadRequest(views.html.createUser(bindedFormRequest.withGlobalError(error.message)))
      }
    )

  }
  def getCreate(token:String) = messagesAction.async {implicit request: MessagesRequest[AnyContent]=>
    implicit val nonBlockingDispatcher = akkaDispatcherProvider.nonBlockingDispatcher
    service.progressCreateUserFromToken(token).map{
      case Right(createToken) => Ok(views.html.createUser(createUserForm)).withSession(("create_token",createToken))
      case Left(error)=> BadRequest(error.message)
    }

  }

  def getCreateStartRequest = messagesAction {implicit request: MessagesRequest[AnyContent]=>
    Ok(views.html.startCreateUser(startCreateUserRequestForm))
  }

  def postCreateStartRequest = messagesAction.async { implicit request =>

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
    val url = routes.UserController.getCreate(requestKey).absoluteURL()(request)
    val body = views.html.userRegistrationMail(url).body

    //メール送信完了まで待機してしまうと、レスポンスが非常に遅くなってしまうため、待機しない
    mailService.sendMail(mailTo,"ユーザ登録通知", body)
  }


}
