import java.nio.file.Files
import java.nio.file.StandardCopyOption

organization in ThisBuild := "com.lightbend.lagom.sample.chirper"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.11.12"

// SCALA SUPPORT: Remove the line below
EclipseKeys.projectFlavor in Global := EclipseProjectFlavor.Java

lazy val buildVersion = sys.props.getOrElse("buildVersion", "1.0.0-SNAPSHOT")

version in ThisBuild := buildVersion

lazy val friendApi = project("friend-api")
  .settings(
    version := buildVersion,
    libraryDependencies += lagomJavadslApi
  )

lazy val friendImpl = project("friend-impl")
  .enablePlugins(LagomJava, SbtReactiveAppPlugin)
  .settings(
    libraryDependencies ++= Seq(
      lagomJavadslPersistenceCassandra,
      lagomJavadslTestKit
    )
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(friendApi)

lazy val chirpApi = project("chirp-api")
  .settings(
    libraryDependencies ++= Seq(
      lagomJavadslApi,
      lagomJavadslJackson
    )
  )

lazy val chirpImpl = project("chirp-impl")
  .enablePlugins(LagomJava, SbtReactiveAppPlugin)
  .settings(
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
    version in Docker := buildVersion,
    libraryDependencies += lagomJavadslApi
  )
  .dependsOn(chirpApi)

lazy val activityStreamImpl = project("activity-stream-impl")
  .enablePlugins(LagomJava, SbtReactiveAppPlugin)
  .settings(
    libraryDependencies ++= Seq(
      lagomJavadslCluster,
      lagomJavadslTestKit
    )
  )
  .dependsOn(activityStreamApi, chirpApi, friendApi)

lazy val frontEnd = project("front-end")
  .enablePlugins(PlayJava, LagomPlay, SbtReactiveAppPlugin)
  .disablePlugins(PlayLayoutPlugin)
  .settings(
    routesGenerator := InjectedRoutesGenerator,
    libraryDependencies ++= Seq(
      "org.webjars" % "foundation" % "5.5.2",
      "org.webjars" %% "webjars-play" % "2.5.0",
      lagomJavadslClient
    ),
    includeFilter in webpack := "*.js" || "*.jsx",
    compile in Compile := (compile in Compile).dependsOn(webpack.toTask("")).value,
    mappings in (Compile, packageBin) := {
      val compiledJsFiles = (WebKeys.public in Assets).value.listFiles().toSeq

      val publicJsFileMappings = compiledJsFiles.map { jsFile =>
        jsFile -> s"public/${jsFile.getName}"
      }

      val webJarsPathPrefix = SbtWeb.webJarsPathPrefix.value
      val compiledWebJarsBaseDir = (classDirectory in Assets).value / webJarsPathPrefix
      val compiledFilesWebJars = compiledJsFiles.map { compiledJs =>
        val compiledJsWebJar = compiledWebJarsBaseDir / compiledJs.getName
        Files.copy(compiledJs.toPath, compiledJsWebJar.toPath, StandardCopyOption.REPLACE_EXISTING)
        compiledJsWebJar
      }
      val webJarJsFileMappings = compiledFilesWebJars.map { jsFile =>
        jsFile -> s"${webJarsPathPrefix}/${jsFile.getName}"
      }

      (mappings in (Compile, packageBin)).value ++ publicJsFileMappings ++ webJarJsFileMappings
    },
    sourceDirectory in Assets := baseDirectory.value / "src" / "main" / "resources" / "assets",
    resourceDirectory in Assets := baseDirectory.value / "src" / "main" / "resources" / "public",

    PlayKeys.playMonitoredFiles ++=
      (sourceDirectories in (Compile, TwirlKeys.compileTemplates)).value :+
      (sourceDirectory in Assets).value :+
      (resourceDirectory in Assets).value,

    WebpackKeys.envVars in webpack += "BUILD_SYSTEM" -> "sbt",

    // Remove to use Scala IDE
    EclipseKeys.createSrc := EclipseCreateSrc.ValueSet(EclipseCreateSrc.ManagedClasses, EclipseCreateSrc.ManagedResources)
  )

lazy val loadTestApi = project("load-test-api")
  .settings(
    libraryDependencies += lagomJavadslApi
  )

lazy val loadTestImpl = project("load-test-impl")
  .enablePlugins(LagomJava, SbtReactiveAppPlugin)
  .dependsOn(loadTestApi, friendApi, activityStreamApi, chirpApi)

def project(id: String) = Project(id, base = file(id))
  .settings(javacOptions in compile ++= Seq("-encoding", "UTF-8", "-source", "1.8", "-target", "1.8", "-Xlint:unchecked", "-Xlint:deprecation"))
  .settings(jacksonParameterNamesJavacSettings: _*) // applying it to every project even if not strictly needed.


// See https://github.com/FasterXML/jackson-module-parameter-names
lazy val jacksonParameterNamesJavacSettings = Seq(
  javacOptions in compile += "-parameters"
)

// do not delete database files on start
lagomCassandraCleanOnStart in ThisBuild := false

// Kafka can be disabled until we need it
lagomKafkaEnabled in ThisBuild := false

licenses in ThisBuild := Seq("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0"))
