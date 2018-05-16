package lagom.demo.account.impl

import com.lightbend.lagom.scaladsl.persistence.ReadSideProcessor
import com.lightbend.lagom.scaladsl.persistence.slick.SlickReadSide

class AccountEventsProcessor(readSide: SlickReadSide,
                             repository: AccountReportRepository) extends ReadSideProcessor[AccountEvent] {

  override def buildHandler() =
    readSide
      .builder[AccountEvent]("account-report")
      .setGlobalPrepare(repository.createTable())
      .setEventHandler[Deposited] { evt =>
        repository.increase(evt.entityId)
      }
      .setEventHandler[Withdrawn] { evt =>
        repository.increase(evt.entityId)
      }
      .build()

  override def aggregateTags = AccountEvent.ShardedTags.allTags
}
