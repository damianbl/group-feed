lazy val akkaHttpVersion = "10.1.7"
lazy val akkaVersion = "2.5.21"
lazy val h2Version = "1.4.198"
lazy val slickVersion = "3.3.0"
lazy val typesafeConfigVersion = "1.3.3"

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

      "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
      "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
      "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test,
      "org.scalatest" %% "scalatest" % "3.0.5" % Test
    )
  )
