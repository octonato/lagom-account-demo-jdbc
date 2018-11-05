package lagom.demo.account.impl

import com.lightbend.lagom.scaladsl.persistence.ReadSideProcessor
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraReadSide
import org.slf4j.LoggerFactory

class AccountEventsProcessor(readSide: CassandraReadSide,
                             repository: AccountReportRepository) extends ReadSideProcessor[AccountEvent] {


  private val logger = LoggerFactory.getLogger(this.getClass)

  override def buildHandler() =
    readSide
      .builder[AccountEvent]("account-report")
      .setGlobalPrepare(repository.createTable)
      .setPrepare(_ => repository.prepareStatements())
      .setEventHandler[Deposited] { evt =>
        logger.info(s"Deposit event: $evt")
        repository.increase(evt.entityId)
      }
      .setEventHandler[Withdrawn] { evt =>
        logger.info(s"Withdrawn event: $evt")
        repository.increase(evt.entityId)
      }
      .build()

  override def aggregateTags = AccountEvent.ShardedTags.allTags
}
