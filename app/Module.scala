import com.google.inject.AbstractModule
import java.time.Clock

import services._


class Module extends AbstractModule {

  override def configure() = {

    bind(classOf[UserService]).to(classOf[UserServiceImpl])
  }

}
