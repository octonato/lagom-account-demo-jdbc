package lagom.demo.account.impl

import java.time.LocalDateTime

import akka.Done
import com.lightbend.lagom.scaladsl.persistence._
import com.lightbend.lagom.scaladsl.persistence.PersistentEntity.ReplyType
import com.lightbend.lagom.scaladsl.playjson.{JsonSerializer, JsonSerializerRegistry}
import com.lightbend.lagom.scaladsl.pubsub.{PubSubRegistry, TopicId}
import org.slf4j.LoggerFactory
import play.api.libs.json.{Format, Json}

import scala.collection.immutable.Seq

class AccountEntity(pubSub: PubSubRegistry) extends PersistentEntity {

  override type Command = AccountCommand[_]
  override type Event = AccountEvent
  override type State = Account

  override def initialState = Account(0.0)

  override def behavior = {
    case Account(balance) =>
      Actions()
        .onCommand[Deposit, Done] {
          case (Deposit(amount), ctx, _) =>
            ctx.thenPersist(Deposited(amount)) { _ =>
              pubSub.refFor(TopicId[String](entityId)).publish("Deposited")
              ctx.reply(Done)
            }
        }
        .onCommand[Withdraw, Done] {
          case (Withdraw(amount), ctx, _) =>
            if (balance - amount >= 0) {
              ctx.thenPersist(Withdrawn(amount)) { _ =>
                pubSub.refFor(TopicId[String](entityId)).publish("Withdrawn")
                ctx.reply(Done)
              }
            } else {
              ctx.invalidCommand(s"Insufficient balance. Can't withdraw $amount")
              ctx.done
            }
        }
        .onEvent {
          case (Deposited(amount), state) => state.copy(balance + amount)
          case (Withdrawn(amount), state) => state.copy(balance - amount)
        }
        .onReadOnlyCommand[GetBalance.type , Double] {
          case (GetBalance, ctx, state) => ctx.reply(state.balance)
        }
  }
}

case class Account(balance: Double)

object Account {
  implicit val format = Json.format[Account]
}

sealed trait AccountCommand[R] extends ReplyType[R]

case object GetBalance extends AccountCommand[Double]

case class Deposit(amount: Double) extends AccountCommand[Done]
object Deposit {
  implicit val format = Json.format[Deposit]
}

case class Withdraw(amount: Double) extends AccountCommand[Done]
object Withdraw {
  implicit val format = Json.format[Withdraw]
}

sealed trait AccountEvent extends AggregateEvent[AccountEvent] {
  override def aggregateTag: AggregateEventTagger[AccountEvent] = AccountEvent.ShardedTags
}

object AccountEvent {
  val NumShards = 10
  val ShardedTags = AggregateEventTag.sharded[AccountEvent]("AccountEvent", NumShards)
}

case class Deposited(amount: Double) extends AccountEvent

object Deposited {
  implicit val format = Json.format[Deposited]
}
case class Withdrawn(amount: Double) extends AccountEvent

object Withdrawn {
  implicit val format = Json.format[Withdrawn]
}


object AccountSerializerRegistry extends JsonSerializerRegistry {

  override def serializers: Seq[JsonSerializer[_]] = Seq(
    JsonSerializer[Account],
    JsonSerializer[Deposit],
    JsonSerializer[Withdraw],
    JsonSerializer[Deposited],
    JsonSerializer[Withdrawn]
  )
}
