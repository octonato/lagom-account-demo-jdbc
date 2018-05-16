package lagom.demo.account.impl

import akka.Done
import slick.dbio.DBIO
import slick.jdbc.PostgresProfile.api._
import slick.jdbc.meta.MTable

import scala.concurrent.ExecutionContext.Implicits.global

class AccountReportRepository {

  case class AccountReport(accountNumber: String, txCount: Int) {
    def increaseCount(): AccountReport = this.copy(txCount = txCount + 1)
  }

  class ReportTable(tag: Tag) extends Table[AccountReport](tag, "account_report") {

    def accountNumber = column[String]("account_number", O.PrimaryKey)
    def txCount = column[Int]("tx_count")

    def * = (accountNumber, txCount) <> (AccountReport.tupled, AccountReport.unapply)
  }

  val reportTable = TableQuery[ReportTable]

  def createTable() ={
    MTable.getTables.flatMap { tables =>
      if (!tables.exists(_.name.name == reportTable.baseTableRow.tableName)) {
        reportTable.schema.create.map(_ => Done)
      } else {
        DBIO.successful(Done)
      }
    }.transactionally

  }

  def increase(accountNumber: String): DBIO[Done] = {
    for {
      report <- this.findByNumber(accountNumber)
      _ <- this.save(report.increaseCount())
    } yield Done
  }

  def findByNumber(accountNumber: String): DBIO[AccountReport] =
    reportTable
      .filter(_.accountNumber === accountNumber)
      .result
      .headOption
      .map {
        case Some(report) => report
        case None => AccountReport(accountNumber, 0)
      }

  def save(report: AccountReport): DBIO[Done] =
    reportTable.insertOrUpdate(report).map(_ => Done)

}


