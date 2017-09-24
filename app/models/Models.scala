package models

import java.sql.Timestamp

case class User(id:Long, mail:String, passwordHash:String, name:String, createdAt:Timestamp,updatedAt:Timestamp,deletedAt:Timestamp)