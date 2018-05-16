package lagom.demo.account.impl

import lagom.demo.account.api
import lagom.demo.account.api.AccountService
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.broker.TopicProducer
import com.lightbend.lagom.scaladsl.persistence.{EventStreamElement, PersistentEntityRegistry}
import org.slf4j.LoggerFactory
import slick.jdbc.JdbcBackend.DatabaseDef
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AccountServiceImpl (persistentEntityRegistry: PersistentEntityRegistry,
                          accountRepository: AccountReportRepository,
                          db: DatabaseDef ) extends AccountService {

  val logger = LoggerFactory.getLogger(getClass)

  override def deposit(accountNumber: String) = ServiceCall { req =>
    val ref = persistentEntityRegistry.refFor[AccountEntity](accountNumber)
    logger.info(s"make a deposit of ${req.amount} on $accountNumber")
    ref.ask(Deposit(req.amount))
  }


  override def withdraw(accountNumber: String) = ServiceCall { req =>
    val ref = persistentEntityRegistry.refFor[AccountEntity](accountNumber)
    logger.info(s"make a withdraw of ${req.amount} on $accountNumber")
    ref.ask(Withdraw(req.amount))
  }

  override def balance(accountNumber: String) = ServiceCall { _ =>
    val ref = persistentEntityRegistry.refFor[AccountEntity](accountNumber)
    logger.info(s"get balance for $accountNumber")
    ref.ask(GetBalance)
  }

  override def transactionCount(accountNumber: String) = ServiceCall { _ =>
    db.run(accountRepository.findByNumber(accountNumber)).map(_.txCount)
  }

}
