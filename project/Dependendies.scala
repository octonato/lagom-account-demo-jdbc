
import sbt._
import sbt.Keys._
object Dependencies {

  val reactiveLibVersion = "0.9.3-beta"
  val reactiveLib = Seq(
    "com.lightbend.rp" %% "reactive-lib-common" % reactiveLibVersion,
    "com.lightbend.rp" %% "reactive-lib-play-http-binding" % reactiveLibVersion,
    "com.lightbend.rp" %% "reactive-lib-common" % reactiveLibVersion,
    "com.lightbend.rp" %% "reactive-lib-status" % reactiveLibVersion,
    "com.lightbend.rp" %% "reactive-lib-akka-management" % reactiveLibVersion,
    "com.lightbend.rp" %% "reactive-lib-service-discovery-lagom14-scala" % reactiveLibVersion,
    "com.lightbend.rp" %% "reactive-lib-service-discovery" % reactiveLibVersion,
    "com.lightbend.rp" %% "reactive-lib-async-dns" % reactiveLibVersion
  )

}