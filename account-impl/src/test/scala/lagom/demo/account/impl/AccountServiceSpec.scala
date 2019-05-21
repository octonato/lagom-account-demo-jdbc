package lagom.demo.account.impl

import com.lightbend.lagom.scaladsl.server.LocalServiceLocator
import com.lightbend.lagom.scaladsl.testkit.ServiceTest
import org.scalatest.AsyncWordSpec
import org.scalatest.Matchers
class HelloServiceSpec extends AsyncWordSpec with Matchers {

  "The HelloService" should {
    "say hello" in ServiceTest.withServer(ServiceTest.defaultSetup) { ctx =>
      new AccountApplication(ctx) with LocalServiceLocator
    } { server =>
      val client = server.serviceClient.implement[HelloService]

      client.sayHello.invoke("Alice").map { response =>
        response should ===("Hello Alice!")
      }
    }
  }
}