package utilities

case class ApplicationError (message:String,exception:Exception = null)