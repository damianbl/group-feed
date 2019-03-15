val akkaHttpVersion = "10.1.7"
val akkaVersion = "2.5.21"
val hsqlVersion = "2.4.0"
val slickVersion = "3.3.0"
val typesafeConfigVersion = "1.3.3"
val logbackClassicVersion = "1.2.3"
val scalaLoggingVersion = "3.9.2"
val scalazVersion = "7.2.27"
val scalaTestVersion = "3.0.5"
val scalaMockVersion = "4.1.0"

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.dblazejewski",
      scalaVersion := "2.12.8"
    )),

    name := "group-feed",

    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-xml" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-stream" % akkaVersion,

      "com.typesafe.slick" %% "slick" % slickVersion,
      "com.typesafe.slick" %% "slick-hikaricp" % slickVersion,

      "org.hsqldb" % "hsqldb" % hsqlVersion,

      "com.typesafe" % "config" % typesafeConfigVersion,

      "ch.qos.logback" % "logback-classic" % logbackClassicVersion,
      "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion,

      "org.scalaz" %% "scalaz-core" % scalazVersion,

      "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
      "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
      "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test,
      "org.scalatest" %% "scalatest" % scalaTestVersion % Test,
      "org.scalamock" %% "scalamock" % scalaMockVersion % Test
    )
  )
