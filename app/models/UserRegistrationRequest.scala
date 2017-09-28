package models

import java.time.LocalDateTime

import play.api.libs.json._

case class UserRegistrationRequest(mail:String, expiresAt:LocalDateTime)


object UserRegistrationRequest {
  implicit val readUserRegistrationRequest = Json.format[UserRegistrationRequest]

}
