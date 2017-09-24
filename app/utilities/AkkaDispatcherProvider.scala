package utilities


import javax.inject.Inject

import play.api.libs.concurrent.{ActorSystemProvider, Akka}
import play.components.AkkaComponents

class AkkaDispatcherProvider @Inject()(akkaSystemProvider: ActorSystemProvider) {
  def blockingDispatcher = akkaSystemProvider.get.dispatchers.lookup("blocking-io-dispatcher")
  def nonBlockingDispatcher = akkaSystemProvider.get.dispatchers.lookup("non-blocking-io-dispatcher")
}
