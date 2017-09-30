package utilities

import javax.inject.Inject

import com.google.inject.{ImplementedBy, Provider}
import play.Application

@ImplementedBy(classOf[ApplicationSettingImpl])
trait ApplicationSetting {
   def userRegistrationRequestKey :String
   def userPasswordHashKey :String
}

class ApplicationSettingImpl @Inject()(applicationProvider:Provider[Application]) extends ApplicationSetting{
  private val config = applicationProvider.get().config()
  def userRegistrationRequestKey = config.getString("userRegistrationRequestKey")
  def userPasswordHashKey = config.getString("userPasswordHashKey")
}
