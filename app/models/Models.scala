package models

import java.sql.Timestamp
import java.time.LocalDateTime

case class User(id:Int, mail:String, password:String, name:String, createdAt:LocalDateTime,updatedAt:LocalDateTime)

case class CreateUserRequestMailToken(token:String)

case class CreateUserToken(mail:String)

case class Status(id:Int,userID:Int,text:String,createdAt:LocalDateTime,updatedAt:LocalDateTime)



