package lagom.demo.account.impl

import akka.Done
import com.lightbend.lagom.scaladsl.persistence.cassandra.CassandraSession
import akka.stream.Materializer
import com.datastax.driver.core.{BoundStatement, PreparedStatement}

import scala.concurrent.{ExecutionContext, Future}
import scala.collection.immutable
class AccountReportRepository(session: CassandraSession)(implicit ec: ExecutionContext, mat: Materializer) {

  case class AccountReport(accountNumber: String, txCount: Int) {
    def increaseCount(): AccountReport = this.copy(txCount = txCount + 1)
  }


  private var insertAccountReport: PreparedStatement = _


  def prepareStatements(): Future[Done] = {
    for {
      insert <- session.prepare("INSERT INTO accountReports(accountNumber, txCount) VALUES (?, ?)")
    } yield {
      insertAccountReport = insert
      Done
    }
  }

  def createTable(): Future[Done] = {
    for {
      _ <- session.executeCreateTable("""
          CREATE TABLE IF NOT EXISTS accountReports (
            accountNumber text,
            txCount int,
            PRIMARY KEY (accountNumber)
          )
      """)
    } yield Done
  }

  def increase(accountNumber: String): Future[immutable.Seq[BoundStatement]] = {

    findByNumber(accountNumber).map {
      case Some(report) => save(report.increaseCount())
      case None => immutable.Seq.empty[BoundStatement]
    }
  }

  def findByNumber(accountNumber: String): Future[Option[AccountReport]] = {

      session.selectOne(s"""
        SELECT
          accountNumber, txCount
        FROM
          accountReports
        WHERE
          accountNumber = ?
        """,
        accountNumber).map { rowOpt =>
          rowOpt.map(row => AccountReport(row.getString("accountNumber"), row.getInt("txCount")))
        }
  }
  def save(report: AccountReport): immutable.Seq[BoundStatement] = {
    val ps = insertAccountReport.bind()
    ps.setString("accountNumber", report.accountNumber)
    ps.setInt("txCount", report.txCount)
    List(ps)
  }



}


