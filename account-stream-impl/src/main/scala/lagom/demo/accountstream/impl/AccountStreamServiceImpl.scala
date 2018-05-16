package lagom.demo.accountstream.impl

import akka.{Done, NotUsed}
import akka.stream.scaladsl.{Flow, Source}
import com.lightbend.lagom.scaladsl.api.ServiceCall
import lagom.demo.account.api.AccountService
import lagom.demo.accountstream.api.AccountStreamService
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Implementation of the AccountStreamService.
  */
class AccountStreamServiceImpl(accountService: AccountService) extends AccountStreamService {


  accountService
    .transactions
    .subscribe
    .atLeastOnce(
      Flow[String].map { msg =>
        println(
          s"""
             | message on topic: $msg
             | -----------------------------------------
                """.stripMargin)
        Done
      }
    )

  override def stream(accountNumber: String): ServiceCall[NotUsed, Source[String, NotUsed]] = ServiceCall { _ =>
    accountService
      .transactionsForAccount(accountNumber)
      .invoke()
      .map(_.map( s => s"streaming: $s"))
  }
}
