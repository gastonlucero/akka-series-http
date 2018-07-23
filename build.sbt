lazy val akkaHttpVersion = "10.1.3"
lazy val igniteVersion    = "2.6.0"

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization    := "com.akkaseries",
      scalaVersion    := "2.12.3"
    )),
    name := "akka-series-http",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http"            % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
      "org.apache.ignite" % "ignite-core"           % igniteVersion,
      "com.typesafe.akka" %% "akka-http-testkit"    % akkaHttpVersion % Test,
      "org.scalatest"     %% "scalatest"            % "3.0.1"         % Test
    )
  )
