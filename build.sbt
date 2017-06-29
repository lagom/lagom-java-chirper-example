import java.nio.file.Files
import java.nio.file.StandardCopyOption
import sbt.Resolver.bintrayRepo
import com.typesafe.sbt.web.SbtWeb
import com.typesafe.sbt.packager.docker._

organization in ThisBuild := "com.lightbend.lagom.sample.chirper"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.11.8"

// SCALA SUPPORT: Remove the line below
EclipseKeys.projectFlavor in Global := EclipseProjectFlavor.Java

lazy val friendApi = project("friend-api")
  .settings(
    version := "1.0-SNAPSHOT",
    libraryDependencies += lagomJavadslApi
  )

lazy val friendImpl = project("friend-impl")
  .enablePlugins(LagomJava)
  .settings(
    version := "1.0-SNAPSHOT",
    dockerRepository := Some("chirper"),
    dockerUpdateLatest := true,
    dockerEntrypoint ++= """-Dplay.crypto.secret="${APPLICATION_SECRET:-none}" -Dplay.akka.actor-system="${AKKA_ACTOR_SYSTEM_NAME:-friendservice-v1}" -Dhttp.address="$FRIENDSERVICE_BIND_IP" -Dhttp.port="$FRIENDSERVICE_BIND_PORT" -Dakka.actor.provider=cluster -Dakka.remote.netty.tcp.hostname="$(eval "echo $AKKA_REMOTING_BIND_HOST")" -Dakka.remote.netty.tcp.port="$AKKA_REMOTING_BIND_PORT" -Dakka.cluster.seed-nodes.0="akka.tcp://${AKKA_ACTOR_SYSTEM_NAME}@${AKKA_SEED_NODE_HOST}:${AKKA_SEED_NODE_PORT}" -Dakka.io.dns.resolver=async-dns -Dakka.io.dns.async-dns.resolve-srv=true -Dakka.io.dns.async-dns.resolv-conf=on""".split(" ").toSeq,
    dockerCommands :=
      dockerCommands.value.flatMap {
        case ExecCmd("ENTRYPOINT", args @ _*) => Seq(Cmd("ENTRYPOINT", args.mkString(" ")))
        case v => Seq(v)
      },
    resolvers += bintrayRepo("hajile", "maven"),
    libraryDependencies ++= Seq(
      lagomJavadslPersistenceCassandra,
      lagomJavadslTestKit
    ) ++ BuildTarget.additionalLibraryDependencies
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
    dockerRepository := Some("chirper"),
    dockerUpdateLatest := true,
    dockerEntrypoint ++= """-Dplay.crypto.secret="${APPLICATION_SECRET:-none}" -Dplay.akka.actor-system="${AKKA_ACTOR_SYSTEM_NAME:-chirpservice-v1}" -Dhttp.address="$CHIRPSERVICE_BIND_IP" -Dhttp.port="$CHIRPSERVICE_BIND_PORT" -Dakka.actor.provider=cluster -Dakka.remote.netty.tcp.hostname="$(eval "echo $AKKA_REMOTING_BIND_HOST")" -Dakka.remote.netty.tcp.port="$AKKA_REMOTING_BIND_PORT" -Dakka.cluster.seed-nodes.0="akka.tcp://${AKKA_ACTOR_SYSTEM_NAME}@${AKKA_SEED_NODE_HOST}:${AKKA_SEED_NODE_PORT}" -Dakka.io.dns.resolver=async-dns -Dakka.io.dns.async-dns.resolve-srv=true -Dakka.io.dns.async-dns.resolv-conf=on""".split(" ").toSeq,
    dockerCommands :=
      dockerCommands.value.flatMap {
        case ExecCmd("ENTRYPOINT", args @ _*) => Seq(Cmd("ENTRYPOINT", args.mkString(" ")))
        case v => Seq(v)
      },
    resolvers += bintrayRepo("hajile", "maven"),
    libraryDependencies ++= Seq(
      lagomJavadslPersistenceCassandra,
      lagomJavadslPubSub,
      lagomJavadslTestKit
    ) ++ BuildTarget.additionalLibraryDependencies
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
    dockerRepository := Some("chirper"),
    dockerUpdateLatest := true,
    dockerEntrypoint ++= """-Dplay.crypto.secret="${APPLICATION_SECRET:-none}" -Dplay.akka.actor-system="${AKKA_ACTOR_SYSTEM_NAME:-activityservice-v1}" -Dhttp.address="$ACTIVITYSERVICE_BIND_IP" -Dhttp.port="$ACTIVITYSERVICE_BIND_PORT" -Dakka.actor.provider=cluster -Dakka.remote.netty.tcp.hostname="$(eval "echo $AKKA_REMOTING_BIND_HOST")" -Dakka.remote.netty.tcp.port="$AKKA_REMOTING_BIND_PORT" -Dakka.cluster.seed-nodes.0="akka.tcp://${AKKA_ACTOR_SYSTEM_NAME}@${AKKA_SEED_NODE_HOST}:${AKKA_SEED_NODE_PORT}" -Dakka.io.dns.resolver=async-dns -Dakka.io.dns.async-dns.resolve-srv=true -Dakka.io.dns.async-dns.resolv-conf=on""".split(" ").toSeq,
    dockerCommands :=
      dockerCommands.value.flatMap {
        case ExecCmd("ENTRYPOINT", args @ _*) => Seq(Cmd("ENTRYPOINT", args.mkString(" ")))
        case v => Seq(v)
      },
    resolvers += bintrayRepo("hajile", "maven"),
    libraryDependencies ++= Seq(
      lagomJavadslCluster,
      lagomJavadslTestKit
    ) ++ BuildTarget.additionalLibraryDependencies
  )
  .dependsOn(activityStreamApi, chirpApi, friendApi)

lazy val frontEnd = project("front-end")
  .enablePlugins(PlayJava, LagomPlay)
  .disablePlugins(PlayLayoutPlugin)
  .settings(
    version := "1.0-SNAPSHOT",
    routesGenerator := InjectedRoutesGenerator,
    dockerRepository := Some("chirper"),
    dockerUpdateLatest := true,
    dockerEntrypoint ++= """-Dplay.crypto.secret="${APPLICATION_SECRET:-none}" -Dhttp.address="$WEB_BIND_IP" -Dhttp.port="$WEB_BIND_PORT"""".split(" ").toSeq,
    dockerCommands :=
      dockerCommands.value.flatMap {
        case ExecCmd("ENTRYPOINT", args @ _*) => Seq(Cmd("ENTRYPOINT", args.mkString(" ")))
        case v => Seq(v)
      },
    resolvers += bintrayRepo("hajile", "maven"),
    libraryDependencies ++= Seq(
      "org.webjars" % "foundation" % "5.5.2",
      "org.webjars" %% "webjars-play" % "2.5.0",
      lagomJavadslClient
    ) ++ BuildTarget.additionalLibraryDependencies,

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
    version := "1.0-SNAPSHOT",
    libraryDependencies += lagomJavadslApi
  )

lazy val loadTestImpl = project("load-test-impl")
  .enablePlugins(LagomJava)
  .settings(
    version := "1.0-SNAPSHOT",
    resolvers += bintrayRepo("hajile", "maven"),
    libraryDependencies ++= BuildTarget.additionalLibraryDependencies
   )
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
