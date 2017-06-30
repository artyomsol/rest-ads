import _root_.sbtbuildinfo.BuildInfoPlugin
import _root_.sbtbuildinfo.BuildInfoPlugin.autoImport._
import com.typesafe.sbt.GitPlugin.autoImport._
import com.typesafe.sbt.{GitBranchPrompt, GitVersioning}

name := "rest-ads"

scalaVersion := "2.11.8"

resolvers ++= Seq (
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
  DefaultMavenRepository
)

libraryDependencies ++= {
  val akkaHttpVersion = "10.0.9"
  val elastic4sVersion = "5.4.5"
  val scalaTestVersion = "3.0.1"
  val akkaVersion = "2.4.19"
  Seq(
    "com.typesafe.akka" %% "akka-http-core" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
    "com.sksamuel.elastic4s" %% "elastic4s-core" % elastic4sVersion,
    "com.sksamuel.elastic4s" %% "elastic4s-tcp" % elastic4sVersion,
    "com.sksamuel.elastic4s" %% "elastic4s-streams" % elastic4sVersion,
    "joda-time" % "joda-time" % "2.9.+",
    "org.joda" % "joda-convert" % "1.8.+",
    "ch.qos.logback" % "logback-classic" % "1.2.3",
    "org.scalatest" %% "scalatest" % scalaTestVersion % Test,
    "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
    "org.mockito" % "mockito-core" % "2.8.+" % Test,
    "com.sksamuel.elastic4s" %% "elastic4s-testkit" % elastic4sVersion % Test,
    "com.sksamuel.elastic4s" %% "elastic4s-embedded" % elastic4sVersion % Test
  )
}

fork := true

fork in Test := true

scalacOptions in Test ++= Seq("-Yrangepos")

scalacOptions := Seq("-feature", "-unchecked", "-deprecation", "-encoding", "utf8", "-target:jvm-1.8", "-Ydead-code")

ivyScala := ivyScala.value map {
  _.copy(overrideScalaVersion = true)
}

// sbt-git version control
lazy val `rest-ads` = (project in file(".")).
  enablePlugins(BuildInfoPlugin, GitVersioning, GitBranchPrompt).
  settings(
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, buildInfoBuildNumber),
    buildInfoPackage := "service.app",
    buildInfoOptions ++= Seq[BuildInfoOption](BuildInfoOption.BuildTime)
  )

git.useGitDescribe := true

git.formattedShaVersion := {
  val base = git.baseVersion.?.value
  val suffix = git.makeUncommittedSignifierSuffix(git.gitUncommittedChanges.value, git.uncommittedSignifier.value)
  git.gitHeadCommit.value map { sha =>
    git.defaultFormatShaVersion(base, "g" + sha.take(7), suffix) // like a git describe reports
  }
}

git.gitTagToVersionNumber := customTagToVersionNumber

git.baseVersion := "0.1.0"

Revolver.settings
enablePlugins(JavaAppPackaging)
enablePlugins(DockerPlugin)

dockerExposedPorts := Seq(9000)
dockerEntrypoint := Seq("bin/%s" format executableScriptName.value, "-Dconfig.resource=docker.conf")