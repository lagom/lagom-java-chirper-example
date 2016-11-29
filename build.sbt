organization in ThisBuild := "sample.chirper"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.11.8"

lazy val friendApi = project("friend-api")
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies += lagomJavadslApi
  )

lazy val friendImpl = project("friend-impl")
  .enablePlugins(LagomJava)
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      lagomJavadslPersistenceCassandra,
      lagomJavadslTestKit
    )
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(friendApi)

lazy val chirpApi = project("chirp-api")
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      lagomJavadslApi,
      lagomJavadslJackson
    )
  )

lazy val chirpImpl = project("chirp-impl")
  .enablePlugins(LagomJava)
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      lagomJavadslPersistenceCassandra,
      lagomJavadslPubSub,
      lagomJavadslTestKit
    )
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(chirpApi)

lazy val activityStreamApi = project("activity-stream-api")
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies += lagomJavadslApi
  )
  .dependsOn(chirpApi)

lazy val activityStreamImpl = project("activity-stream-impl")
  .enablePlugins(LagomJava)
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies += lagomJavadslTestKit
  )
  .dependsOn(activityStreamApi, chirpApi, friendApi)

lazy val frontEnd = project("front-end")
  .enablePlugins(PlayJava, LagomPlay)
  .settings(
    version := "1.0-SNAPSHOT",
    routesGenerator := InjectedRoutesGenerator,
    libraryDependencies ++= Seq(
      "org.webjars" % "react" % "0.14.8",
      "org.webjars" % "react-router" % "1.0.3",
      "org.webjars" % "jquery" % "2.2.4",
      "org.webjars" % "foundation" % "5.5.2"
    ),
    ReactJsKeys.sourceMapInline := true
  )

lazy val loadTestApi = project("load-test-api")
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies += lagomJavadslApi
  )

lazy val loadTestImpl = project("load-test-impl")
  .enablePlugins(LagomJava)
  .settings(version := "1.0-SNAPSHOT")
  .dependsOn(loadTestApi, friendApi, activityStreamApi, chirpApi)

def project(id: String) = Project(id, base = file(id))
  .settings(eclipseSettings: _*)
  .settings(javacOptions in compile ++= Seq("-encoding", "UTF-8", "-source", "1.8", "-target", "1.8", "-Xlint:unchecked", "-Xlint:deprecation"))
  .settings(jacksonParameterNamesJavacSettings: _*) // applying it to every project even if not strictly needed.


// See https://github.com/FasterXML/jackson-module-parameter-names
lazy val jacksonParameterNamesJavacSettings = Seq(
  javacOptions in compile += "-parameters"
)

// Configures sbteclipse to enable Java support only, and disable Scala to remove clutter from the Project View.
// To use this project as a starting point for a mixed Java/Scala Lagom project, see the
// comments marked "SCALA SUPPORT" below.
lazy val eclipseSettings = Seq(

  // SCALA SUPPORT 1: Comment the (Java flavor) and uncomment the (ScalaIDE flavor)
  EclipseKeys.projectFlavor := EclipseProjectFlavor.Java,
  //  EclipseKeys.projectFlavor := EclipseProjectFlavor.ScalaIDE,

  // SCALA SUPPORT 2: Comment the following two lines.
  unmanagedSourceDirectories in Compile := Seq((javaSource in Compile).value),
  unmanagedSourceDirectories in Test := Seq((javaSource in Test).value),


  EclipseKeys.withBundledScalaContainers := false,
  EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource,
  EclipseKeys.eclipseOutput := Some(".target"),
  EclipseKeys.withSource := true,
  EclipseKeys.withJavadoc := true

)

// do not delete database files on start
lagomCassandraCleanOnStart in ThisBuild := false

// Kafka can be disabled until we need it
lagomKafkaEnabled in ThisBuild := false

licenses in ThisBuild := Seq("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0"))

