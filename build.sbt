val akkaHttpVersion = "10.1.7"
val akkaVersion = "2.5.21"
val h2Version = "1.4.198"
val slickVersion = "3.3.0"
val typesafeConfigVersion = "1.3.3"
val logbackClassicVersion = "1.2.3"
val scalaLoggingVersion = "3.9.2"
val scalazVersion = "7.2.27"


lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.dblazejewski",
      scalaVersion := "2.12.7"
    )),
    name := "group-feed",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-xml" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-stream" % akkaVersion,

      "com.typesafe.slick" %% "slick" % slickVersion,
      "com.typesafe.slick" %% "slick-hikaricp" % slickVersion,
      "com.typesafe" % "config" % typesafeConfigVersion,
      "com.h2database" % "h2" % h2Version,
      "ch.qos.logback" % "logback-classic" % logbackClassicVersion,
      "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion,
      "org.scalaz" %% "scalaz-core" % scalazVersion,

      "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
      "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
      "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test,
      "org.scalatest" %% "scalatest" % "3.0.5" % Test
    )
  )
