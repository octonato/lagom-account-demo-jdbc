package lagom.demo.account.impl

import com.lightbend.lagom.scaladsl.api.ServiceLocator
import com.lightbend.lagom.scaladsl.api.ServiceLocator.NoServiceLocator
import com.lightbend.lagom.scaladsl.server._
import com.lightbend.lagom.scaladsl.devmode.LagomDevModeComponents
import play.api.libs.ws.ahc.AhcWSComponents
import lagom.demo.account.api.AccountService
import com.lightbend.lagom.scaladsl.broker.kafka.LagomKafkaComponents
import com.lightbend.lagom.scaladsl.persistence.slick.SlickPersistenceComponents
import com.softwaremill.macwire._
import play.api.db.HikariCPComponents

class AccountLoader extends LagomApplicationLoader {

  override def load(context: LagomApplicationContext): LagomApplication =
    new AccountApplication(context) {
      override def serviceLocator: ServiceLocator = NoServiceLocator
    }

  override def loadDevMode(context: LagomApplicationContext): LagomApplication =
    new AccountApplication(context) with LagomDevModeComponents

  override def describeService = Some(readDescriptor[AccountService])
}

abstract class AccountApplication(context: LagomApplicationContext)
  extends LagomApplication(context)
    with SlickPersistenceComponents
    with LagomKafkaComponents
    with HikariCPComponents
    with AhcWSComponents {


  lazy val accountRepository = wire[AccountReportRepository]

  // Register the JSON serializer registry
  override lazy val jsonSerializerRegistry = AccountSerializerRegistry

  // Register the Account persistent entity
  persistentEntityRegistry.register(wire[AccountEntity])

  // Register a read-side processor
  readSide.register[AccountEvent](wire[AccountEventsProcessor])

  // Bind the service that this server provides
  override lazy val lagomServer = serverFor[AccountService](wire[AccountServiceImpl])
}
