package models

import java.sql.Timestamp

case class User(id:Int, mail:String, password:String, name:String, createdAt:Timestamp,updatedAt:Timestamp)

case class UserRegistrationRequest(requestKey:String,mail:String,expiredAt:Timestamp, createdAt:Timestamp,updatedAt:Timestamp)
