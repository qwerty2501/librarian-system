package models

import java.sql.Timestamp
import java.time.LocalDateTime

case class User(id:Int, mail:String, password:String, name:String, createdAt:Timestamp,updatedAt:Timestamp)

case class UserRegistrationRequest(mail:String,expiredAt:LocalDateTime)
