package forms


case class StartCreateUserRequestForm(mail:String)

case class CreateUserForm(name:String,password:String,passwordConfirm:String)

case class LoginForm(mail:String,password:String)