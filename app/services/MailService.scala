package services

import javax.inject.Inject

import com.google.inject.ImplementedBy
import controllers.routes
import play.api.libs.mailer.{Email, MailerClient}
import utilities.{AkkaDispatcherProvider, ApplicationError}

import scala.concurrent.Future

@ImplementedBy(classOf[MailServiceImpl])
trait MailService {
  def sendMail(mailTo:String,subject:String,body:String):Future[Either[ApplicationError,_]]
}


class MailServiceImpl @Inject()(mailerClient: MailerClient,akkaDispatcherProvider:AkkaDispatcherProvider) extends MailService{

  def sendMail(mailTo:String,subject:String,body:String): Future[Either[ApplicationError,_]] ={
    Future{
      val mail = Email(subject = subject,
        from = "<scalaexamination@gmail.com>",
        to = Seq(s"<${mailTo}>"),
        bodyHtml = Some(body))
      try{
        mailerClient.send(mail)
        Right(Nil)
      } catch {
        case e:Exception => Left(ApplicationError("メール送信に失敗しました",e))
      }
    }(akkaDispatcherProvider.nonBlockingDispatcher)
  }
}