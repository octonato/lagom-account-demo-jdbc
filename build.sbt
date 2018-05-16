organization in ThisBuild := "lagom.demo"
version in ThisBuild := "1.0-SNAPSHOT"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.12.4"

val macwire = "com.softwaremill.macwire" %% "macros" % "2.3.0" % "provided"
val scalaTest = "org.scalatest" %% "scalatest" % "3.0.4" % Test
val postgres = "org.postgresql" % "postgresql" % "42.1.4"

lazy val `account` = (project in file("."))
  .aggregate(`account-api`, `account-impl`, `account-stream-api`, `account-stream-impl`)

lazy val `account-api` = (project in file("account-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )

lazy val `account-impl` = (project in file("account-impl"))
  .enablePlugins(LagomScala, SbtReactiveAppPlugin)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceCassandra,
      lagomScaladslKafkaBroker,
      macwire,
      postgres,
      scalaTest
    )
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`account-api`)

  
lazy val `account-stream-api` = (project in file("account-stream-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi
    )
  )

lazy val `account-stream-impl` = (project in file("account-stream-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslKafkaBroker,
      macwire,
      scalaTest
    )
  )
  .dependsOn(`account-stream-api`, `account-api`)
