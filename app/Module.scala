import com.google.inject.AbstractModule
import java.time.Clock

import dao._
import services._


class Module extends AbstractModule {

  override def configure() = {

    bind(classOf[UserService]).to(classOf[UserServiceImpl])

    bind(classOf[UserDAO]).to(classOf[UserDAOImpl])

    bind(classOf[UserRegistrationRequestDAO]).to(classOf[UserRegistrationRequestDAOImpl])
  }

}
