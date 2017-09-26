package controllers

import java.util.UUID
import javax.inject._

import forms._
import play.api._
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc._
import services.UserService
import utilities.{AkkaDispatcherProvider, ApplicationError}
import play.api.i18n.Messages.Implicits._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.mailer.{Email, MailerClient}

import scala.concurrent.{ExecutionContext, Future}
import ExecutionContext.Implicits.global

@Singleton
class UserController @Inject()(service:UserService,akkaDispatcherProvider:AkkaDispatcherProvider,mailerClient: MailerClient,messagesAction: MessagesActionBuilder) extends InjectedController{

  private val startCreateUserRequestForm = Form(
    mapping(
      "mail"-> email
    )(StartCreateUserRequestForm.apply)(StartCreateUserRequestForm.unapply)
  )

  def create(requestKey:String) = messagesAction {implicit request: MessagesRequest[AnyContent]=>
    Ok(views.html.startCreateUser(startCreateUserRequestForm))
  }

  def startCreate = messagesAction {implicit request: MessagesRequest[AnyContent]=>
    Ok(views.html.startCreateUser(startCreateUserRequestForm))
  }

  def startCreateResult = messagesAction.async { implicit request =>

    val bindedFormRequest = startCreateUserRequestForm.bindFromRequest

    bindedFormRequest.fold(
      formWithErrors =>{
        Future.apply(BadRequest(views.html.startCreateUser(formWithErrors)))
      },
      createUserRequest =>{
        service.userRegistrationRequest(createUserRequest)
          .filter{either => either.isRight}
          .map {either=>
              sendMail(createUserRequest.mail,either.right.get,request)
            }(akkaDispatcherProvider.nonBlockingDispatcher) //メールの送信は非ブロックDispatcherで良い
          .map{
            case Left(error) => BadRequest(views.html.startCreateUser(bindedFormRequest.withGlobalError(error.message,null)))
            case Right(_) => Ok(views.html.startCreateUserResult("ユーザ作成リクエスト","招待メールを送信しました"))
          }(akkaDispatcherProvider.nonBlockingDispatcher)
      }
    )
  }
  def sendMail(mailTo:String, requestKey:UUID,request:Request[_]): Either[ApplicationError,_] ={
    //メール送信自体はModelの機能だが、ユーザからするとメールはViewの機能であり、コントローラにメール送信処理を記述する
    val url = routes.UserController.create(requestKey.toString()).absoluteURL()(request)
    val mail = Email(subject = "ユーザ登録確認メール",
      from = "scala from <scalaexamination@gmail.com>",
      to = Seq(s"mail to <${mailTo}>"),
      bodyHtml = Some(views.html.userRegistrationMail(url).body))
    try{
      mailerClient.send(mail)
      Right(Nil)
    } catch {
      case e:Exception => Left(new ApplicationError("メール送信に失敗しました",e))
    }

  }


}
