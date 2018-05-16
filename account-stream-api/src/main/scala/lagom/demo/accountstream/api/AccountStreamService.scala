package lagom.demo.accountstream.api

import akka.NotUsed
import akka.stream.scaladsl.Source
import com.lightbend.lagom.scaladsl.api.{Service, ServiceCall}

/**
  * The Account stream interface.
  *
  * This describes everything that Lagom needs to know about how to serve and
  * consume the AccountStream service.
  */
trait AccountStreamService extends Service {

  def stream(accountNumber: String): ServiceCall[NotUsed, Source[String, NotUsed]]

  override final def descriptor = {
    import Service._

    named("account-stream")
      .withCalls(
        pathCall("/api/account/:accountNumber/stream", stream _)
      )
      .withAutoAcl(true)
  }
}

