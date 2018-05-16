package lagom.demo.accountstream.impl

import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.server._
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import play.api.libs.ws.ahc.AhcWSComponents
import lagom.demo.account.api.AccountService
import com.softwaremill.macwire._
import lagom.demo.accountstream.api.AccountStreamService
import com.lightbend.lagom.scaladsl.broker.kafka.LagomKafkaClientComponents

class AccountStreamLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new AccountStreamApplication(context) {
      override def serviceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new AccountStreamApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[AccountStreamService])
}

abstract class AccountStreamApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
  with LagomKafkaClientComponents
    with AhcWSComponents {

  // Bind the service that this server provides
  override lazy val lagomServer = serverFor[AccountStreamService](wire[AccountStreamServiceImpl])

  // Bind the AccountService client
  lazy val accountService = serviceClient.implement[AccountService]
}
