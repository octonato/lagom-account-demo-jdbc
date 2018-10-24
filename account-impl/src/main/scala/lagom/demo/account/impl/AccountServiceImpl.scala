package lagom.demo.account.impl

import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry
import lagom.demo.account.api.AccountService
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext.Implicits.global

class AccountServiceImpl (persistentEntityRegistry: PersistentEntityRegistry,
                          accountRepository: AccountReportRepository) extends AccountService {

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
    accountRepository
      .findByNumber(accountNumber)
      .map {
        case Some(acc) => acc.txCount
        case None => 0
      }
  }

}
