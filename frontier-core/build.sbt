name := "frontier-core"

organization := "ws.frontier"

version := "0.1-SNAPSHOT"

scalaVersion := "2.10.1"

resolvers += "Twitter Repo" at "http://maven.twttr.com"

{
    val finagleVersion = "6.2.1"
    libraryDependencies ++= Seq(
        "com.twitter" %% "finagle-core" % finagleVersion withSources(),
        "com.twitter" %% "finagle-native" % finagleVersion withSources(),
        "com.twitter" %% "finagle-redis" % finagleVersion withSources(),
        "com.twitter" %% "finagle-serversets" % finagleVersion withSources(),
        "com.twitter" %% "finagle-http" % finagleVersion withSources()
    )
}

{
  val jacksonVersion = "2.1.4"
  libraryDependencies ++= Seq(
    "com.fasterxml.jackson.core" % "jackson-databind" % jacksonVersion withSources()
  )
}

{
  libraryDependencies ++= Seq(
    "joda-time" % "joda-time" % "1.6.2" withSources(),
    "ch.qos.logback" % "logback-classic" % "1.0.11" withSources()
  )
}

