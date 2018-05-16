package lagom.demo.account.api

import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.api.broker.Topic
import com.lightbend.lagom.scaladsl.api.broker.kafka.{KafkaProperties, PartitionKeyStrategy}
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}
import play.api.libs.json.{Format, Json}

object AccountService  {
  val TOPIC_NAME = "greetings"
}

/**
  * The Account service interface.
  * <p>
  * This describes everything that Lagom needs to know about how to serve and
  * consume the AccountService.
  */
trait AccountService extends Service {

  def balance(accountNumber: String): ServiceCall[NotUsed, Double]

  def transactionCount(accountNumber: String): ServiceCall[NotUsed, Long]

  def deposit(accountNumber: String): ServiceCall[Transaction, Done]

  def withdraw(accountNumber: String): ServiceCall[Transaction, Done]


  override final def descriptor = {
    import Service._
    // @formatter:off
    named("account")
      .withCalls(
        pathCall("/api/account/:accountNumber/balance", balance _),
        pathCall("/api/account/:accountNumber/deposit", deposit _),
        pathCall("/api/account/:accountNumber/withdraw", withdraw _),
        pathCall("/api/account/:accountNumber/txcount", transactionCount _)
      )
      .withAutoAcl(true)
    // @formatter:on
  }
}

case class Transaction(amount: Double)

object Transaction {
  implicit val format: Format[Transaction] = Json.format[Transaction]
}
