package utilities

import java.time.LocalDateTime

import models.UserRegistrationRequest
import pdi.jwt.{JwtAlgorithm, JwtJson}
import play.api.data.validation.ValidationError
import play.api.libs.json._

import scala.util.{Failure, Success, Try}

object JWTHelpers {
  case class JsonErrors(errors: Seq[(JsPath, Seq[JsonValidationError])]) extends Throwable
  def toJWT[T](obj:T,key:String)(implicit formatter:OFormat[T]): String ={
    val claim = Json.toJsObject(obj)
    JwtJson.encode(claim,key,JwtAlgorithm.HS256)
  }

  def fromJWT[T](jwt:String,key:String)(implicit formatter:OFormat[T]):Try[T] ={
    for{
      calim <- JwtJson.decodeJson(jwt,key,Seq(JwtAlgorithm.HS256))
      obj <- Json.fromJson[T](calim).fold(
        errors => Failure(JsonErrors(errors)),
        res => Success(res)
      )
    } yield obj
  }
}
