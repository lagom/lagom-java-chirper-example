import java.nio.file.Files
import java.nio.file.StandardCopyOption

import sbt.Resolver.bintrayRepo
import com.typesafe.sbt.web.SbtWeb
import com.typesafe.sbt.packager.docker._

organization in ThisBuild := "com.lightbend.lagom.sample.chirper"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.12.4"

// SCALA SUPPORT: Remove the line below
EclipseKeys.projectFlavor in Global := EclipseProjectFlavor.Java

lazy val buildVersion = sys.props.getOrElse("buildVersion", "1.0.0-SNAPSHOT")

lazy val friendApi = project("friend-api")
  .settings(
    version := buildVersion,
    libraryDependencies += lagomJavadslApi
  )

lazy val friendImpl = project("friend-impl")
  .enablePlugins(LagomJava)
  .settings(
    version := buildVersion,
    version in Docker := buildVersion,
    dockerBaseImage := "openjdk:8-jre-alpine",
    dockerRepository := Some(BuildTarget.dockerRepository),
    dockerUpdateLatest := true,
    dockerEntrypoint ++= """-Dhttp.address="$(eval "echo $FRIENDSERVICE_BIND_IP")" -Dhttp.port="$(eval "echo $FRIENDSERVICE_BIND_PORT")" -Dakka.remote.netty.tcp.hostname="$(eval "echo $AKKA_REMOTING_HOST")" -Dakka.remote.netty.tcp.bind-hostname="$(eval "echo $AKKA_REMOTING_BIND_HOST")" -Dakka.remote.netty.tcp.port="$(eval "echo $AKKA_REMOTING_PORT")" -Dakka.remote.netty.tcp.bind-port="$(eval "echo $AKKA_REMOTING_BIND_PORT")" $(IFS=','; I=0; for NODE in $AKKA_SEED_NODES; do echo "-Dakka.cluster.seed-nodes.$I=akka.tcp://friendservice@$NODE"; I=$(expr $I + 1); done)""".split(" ").toSeq,
    dockerCommands :=
      dockerCommands.value.flatMap {
        case ExecCmd("ENTRYPOINT", args @ _*) => Seq(Cmd("ENTRYPOINT", args.mkString(" ")))
        case c @ Cmd("FROM", _) => Seq(c, ExecCmd("RUN", "/bin/sh", "-c", "apk add --no-cache bash && ln -sf /bin/bash /bin/sh"))
        case v => Seq(v)
      },
    resolvers += bintrayRepo("hajile", "maven"),
    resolvers += bintrayRepo("hseeberger", "maven"),
    libraryDependencies ++= Seq(
      lagomJavadslPersistenceCassandra,
      lagomJavadslTestKit
    )
  )
  .settings(BuildTarget.additionalSettings)
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(friendApi)

lazy val chirpApi = project("chirp-api")
  .settings(
    version := buildVersion,
    libraryDependencies ++= Seq(
      lagomJavadslApi,
      lagomJavadslJackson
    )
  )

lazy val chirpImpl = project("chirp-impl")
  .enablePlugins(LagomJava)
  .settings(
    version := buildVersion,
    version in Docker := buildVersion,
    dockerBaseImage := "openjdk:8-jre-alpine",
    dockerRepository := Some(BuildTarget.dockerRepository),
    dockerUpdateLatest := true,
    dockerEntrypoint ++= """-Dhttp.address="$(eval "echo $CHIRPSERVICE_BIND_IP")" -Dhttp.port="$(eval "echo $CHIRPSERVICE_BIND_PORT")" -Dakka.remote.netty.tcp.hostname="$(eval "echo $AKKA_REMOTING_HOST")" -Dakka.remote.netty.tcp.bind-hostname="$(eval "echo $AKKA_REMOTING_BIND_HOST")" -Dakka.remote.netty.tcp.port="$(eval "echo $AKKA_REMOTING_PORT")" -Dakka.remote.netty.tcp.bind-port="$(eval "echo $AKKA_REMOTING_BIND_PORT")" $(IFS=','; I=0; for NODE in $AKKA_SEED_NODES; do echo "-Dakka.cluster.seed-nodes.$I=akka.tcp://chirpservice@$NODE"; I=$(expr $I + 1); done)""".split(" ").toSeq,
    dockerCommands :=
      dockerCommands.value.flatMap {
        case ExecCmd("ENTRYPOINT", args @ _*) => Seq(Cmd("ENTRYPOINT", args.mkString(" ")))
        case c @ Cmd("FROM", _) => Seq(c, ExecCmd("RUN", "/bin/sh", "-c", "apk add --no-cache bash && ln -sf /bin/bash /bin/sh"))
        case v => Seq(v)
      },
    resolvers += bintrayRepo("hajile", "maven"),
    resolvers += bintrayRepo("hseeberger", "maven"),
    libraryDependencies ++= Seq(
      lagomJavadslPersistenceCassandra,
      lagomJavadslPubSub,
      lagomJavadslTestKit
    )
  )
  .settings(BuildTarget.additionalSettings)
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(chirpApi)

lazy val activityStreamApi = project("activity-stream-api")
  .settings(
    version := buildVersion,
    version in Docker := buildVersion,
    libraryDependencies += lagomJavadslApi
  )
  .dependsOn(chirpApi)

lazy val activityStreamImpl = project("activity-stream-impl")
  .enablePlugins(LagomJava)
  .settings(
    version := buildVersion,
    version in Docker := buildVersion,
    dockerBaseImage := "openjdk:8-jre-alpine",
    dockerRepository := Some(BuildTarget.dockerRepository),
    dockerUpdateLatest := true,
    dockerEntrypoint ++= """-Dhttp.address="$(eval "echo $ACTIVITYSERVICE_BIND_IP")" -Dhttp.port="$(eval "echo $ACTIVITYSERVICE_BIND_PORT")"""".split(" ").toSeq,
    dockerCommands :=
      dockerCommands.value.flatMap {
        case ExecCmd("ENTRYPOINT", args @ _*) => Seq(Cmd("ENTRYPOINT", args.mkString(" ")))
        case c @ Cmd("FROM", _) => Seq(c, ExecCmd("RUN", "/bin/sh", "-c", "apk add --no-cache bash && ln -sf /bin/bash /bin/sh"))
        case v => Seq(v)
      },
    resolvers += bintrayRepo("hajile", "maven"),
    resolvers += bintrayRepo("hseeberger", "maven"),
    libraryDependencies ++= Seq(
      lagomJavadslCluster,
      lagomJavadslTestKit
    )
  )
  .settings(BuildTarget.additionalSettings)
  .dependsOn(activityStreamApi, chirpApi, friendApi)

lazy val frontEnd = project("front-end")
  .enablePlugins(PlayJava, LagomPlay)
  .disablePlugins(PlayLayoutPlugin)
  .settings(
    version := buildVersion,
    version in Docker := buildVersion,
    routesGenerator := InjectedRoutesGenerator,
    dockerBaseImage := "openjdk:8-jre-alpine",
    dockerRepository := Some(BuildTarget.dockerRepository),
    dockerUpdateLatest := true,
    dockerEntrypoint ++= """-Dhttp.address="$(eval "echo $WEB_BIND_IP")" -Dhttp.port="$(eval "echo $WEB_BIND_PORT")"""".split(" ").toSeq,
    dockerCommands :=
      dockerCommands.value.flatMap {
        case ExecCmd("ENTRYPOINT", args @ _*) => Seq(Cmd("ENTRYPOINT", args.mkString(" ")))
        case c @ Cmd("FROM", _) => Seq(c, ExecCmd("RUN", "/bin/sh", "-c", "apk add --no-cache bash && ln -sf /bin/bash /bin/sh"))
        case v => Seq(v)
      },
    resolvers += bintrayRepo("hajile", "maven"),
    resolvers += bintrayRepo("hseeberger", "maven"),
    libraryDependencies ++= Seq(
      "org.webjars" % "foundation" % "6.4.2",
      "org.webjars" %% "webjars-play" % "2.6.3",
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
  .settings(BuildTarget.additionalSettings)

lazy val loadTestApi = project("load-test-api")
  .settings(
    version := buildVersion,
    libraryDependencies += lagomJavadslApi
  )

lazy val loadTestImpl = project("load-test-impl")
  .enablePlugins(LagomJava)
  .settings(
    version := buildVersion,
    version in Docker := buildVersion,
    resolvers += bintrayRepo("hajile", "maven"),
    resolvers += bintrayRepo("hseeberger", "maven")
   )
  .settings(BuildTarget.additionalSettings)
  .dependsOn(loadTestApi, friendApi, activityStreamApi, chirpApi)

def project(id: String) = Project(id, base = file(id))
  .settings(javacOptions in compile ++= Seq("-encoding", "UTF-8", "-source", "1.8", "-target", "1.8", "-Xlint:unchecked", "-Xlint:deprecation"))
  .settings(jacksonParameterNamesJavacSettings: _*) // applying it to every project even if not strictly needed.
  .settings(
    javaOptions in Universal ++= Seq(
      "-J-XX:+UnlockExperimentalVMOptions",
      "-J-XX:+UseCGroupMemoryLimitForHeap"
    )
  )

// See https://github.com/FasterXML/jackson-module-parameter-names
lazy val jacksonParameterNamesJavacSettings = Seq(
  javacOptions in compile += "-parameters"
)

// do not delete database files on start
lagomCassandraCleanOnStart in ThisBuild := false

// Kafka can be disabled until we need it
lagomKafkaEnabled in ThisBuild := false

licenses in ThisBuild := Seq("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0"))
