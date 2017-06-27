import _root_.sbtbuildinfo.BuildInfoPlugin
import _root_.sbtbuildinfo.BuildInfoPlugin.autoImport._
import com.typesafe.sbt.GitPlugin.autoImport._
import com.typesafe.sbt.{GitBranchPrompt, GitVersioning}
import xerial.sbt.Pack._

name := "rest-ads"

scalaVersion := "2.11.8"

libraryDependencies ++= {
  val akkaVersion = "10.0.9"
  val cassandraDriverVersion = "3.2.0"
  val scalaTestVersion = "3.0.1"
  Seq(
    "com.typesafe.akka" %% "akka-http-core" % akkaVersion,
    "com.typesafe.akka" %% "akka-http" % akkaVersion,
    "com.typesafe.akka" %% "akka-http-spray-json" % akkaVersion,
    "org.scalatest" %% "scalatest" % scalaTestVersion % "test",
    "joda-time" % "joda-time" % "2.9.+",
    "org.joda" % "joda-convert" % "1.8.+",
    "com.typesafe.akka" %% "akka-http-testkit" % akkaVersion % Test
  )
}

fork := true

fork in Test := true

scalacOptions in Test ++= Seq("-Yrangepos")

scalacOptions := Seq("-feature", "-unchecked", "-deprecation", "-encoding", "utf8", "-target:jvm-1.8", "-Ydead-code")

// sbt-git version control
lazy val `rest-ads` = (project in file(".")).
  enablePlugins(BuildInfoPlugin, GitVersioning, GitBranchPrompt).
  settings(
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, buildInfoBuildNumber),
    buildInfoPackage := "app",
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

packAutoSettings

// [Optional] If you used packSettings instead of packAutoSettings,
//  specify mappings from program name -> Main class (full package path)
packMain := Map(name.value -> "Main")

// Add custom settings here
// [Optional] JVM options of scripts (program name -> Seq(JVM option, ...))
// packJvmOpts := Map(name.value -> Seq("-Xmx512m"))

// [Optional] Extra class paths to look when launching a program. You can use ${PROG_HOME} to specify the base directory
//packExtraClasspath := Map("hello" -> Seq("${PROG_HOME}/etc"))

// [Optional] (Generate .bat files for Windows. The default value is true)
packGenerateWindowsBatFile := false


// [Optional] jar file name format in pack/lib folder
//   "default"   (project name)-(version).jar
//   "full"      (organization name).(project name)-(version).jar
//   "no-version" (organization name).(project name).jar
//   "original"  (Preserve original jar file names)
packJarNameConvention := "original"

// [Optional] Patterns of jar file names to exclude in pack
//packExcludeJars := Seq("scala-.*\\.jar")

// [Optional] List full class paths in the launch scripts (default is false) (since 0.5.1)
packExpandedClasspath := false

packCopyDependenciesTarget := target.value / "lib"

packCopyDependenciesUseSymbolicLinks := false

// [Optional] Resource directory mapping to be copied within target/pack. Default is Map("{projectRoot}/src/pack" -> "")
// packResourceDir += (baseDirectory.value / "web" -> "web-content")

// To publish tar.gz, zip archives to the repository, add the following line
// publishPackArchive

// Publish tar.gz archive. To publish another type of archive, use publishPackArchive(xxx) instead
// publishPackArchiveTgz

Revolver.settings