package lagom.demo.accountstream.impl

import akka.Done
import akka.stream.scaladsl.Flow
import com.lightbend.lagom.scaladsl.api.ServiceCall
import lagom.demo.account.api.AccountService
import lagom.demo.accountstream.api.AccountStreamService
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


}
