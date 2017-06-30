logLevel := Level.Warn
resolvers += Classpaths.sbtPluginReleases

addSbtPlugin("io.spray" %% "sbt-revolver" % "0.8.0")
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.6.1")
addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "0.8.5")
addSbtPlugin("com.typesafe.sbt" %% "sbt-native-packager" % "1.1.4")