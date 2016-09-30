name := """portfolio-simulation"""

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.2.4" % "test",
  "org.postgresql" % "postgresql" % "9.4.1209.jre7",
  "log4j" % "log4j" % "1.2.17",
  "joda-time" % "joda-time" % "2.9.4",
  "ch.qos.logback"  %  "logback-classic"   % "1.1.7",
  "org.scalikejdbc" %% "scalikejdbc-test"   % "2.4.2"   % "test",
  "org.scalikejdbc" %% "scalikejdbc" % "2.4.2",
  "com.typesafe" % "config" % "1.3.0",
  "org.flywaydb" % "flyway-core" % "4.0.3",
  "org.scalamock" %% "scalamock-scalatest-support" % "3.2.2" % Test,
  "org.scalatest" %% "scalatest" % "2.2.6" % Test)

javaOptions in (Test, run) ++= Seq("-Dspark.master=local",
  "-Dlog4j.debug=true",
  "-Dlog4j.configuration=log4j.properties")

outputStrategy := Some(StdoutOutput)

fork := true

coverageEnabled in Test:= true


