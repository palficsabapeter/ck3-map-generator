name := "ck3-map-generator"

version := "0.1"

scalaVersion := "2.13.14"

sbtVersion := "1.9.7"

libraryDependencies ++= {
  Seq(
    //logging
    "ch.qos.logback"       % "logback-classic"          % "1.5.6",
    "net.logstash.logback" % "logstash-logback-encoder" % "7.4",
    "org.codehaus.janino"  % "janino"                   % "3.1.12",
    "org.slf4j"            % "jul-to-slf4j"             % "2.0.13",
    //misc
    "com.sksamuel.scrimage" % "scrimage-core"        % "4.1.3",
    "com.github.pureconfig"%% "pureconfig"           % "0.17.6",
    "org.typelevel"        %% "cats-core"            % "2.10.0",
    "org.scalameta"        %% "scalafmt-dynamic"     % "3.8.1",
    "com.lihaoyi"          %% "mainargs"             % "0.7.0",
    "com.github.pathikrit" %% "better-files-akka"    % "3.9.2",
    "org.nd4j"              % "nd4j-api"             % "1.0.0-beta7",
    "org.nd4j"              % "nd4j-native-platform" % "1.0.0-beta7",
  )
}

enablePlugins(JavaAppPackaging)
enablePlugins(BuildInfoPlugin)

lazy val root = project in file(".")

addCommandAlias("check", "fmtCheck stage")
addCommandAlias(
  "fmtCheck",
  "all scalafmtSbtCheck scalafmtCheck",
)
addCommandAlias("fmt", "all scalafmtSbt scalafmt")

lazy val buildTime                       = java.time.ZonedDateTime.now(java.time.ZoneOffset.UTC)
lazy val builtAtMillis: SettingKey[Long] = SettingKey[Long]("builtAtMillis", "time of build")
ThisBuild / builtAtMillis := buildTime.toInstant.toEpochMilli
lazy val builtAtString: SettingKey[String] = SettingKey[String]("builtAtString", "time of build")
ThisBuild / builtAtString := buildTime.toString

buildInfoKeys := Seq[BuildInfoKey](
  name,
  version,
  scalaVersion,
  sbtVersion,
  BuildInfoKey.action("commitHash") {
    git.gitHeadCommit.value
  },
  builtAtString,
  builtAtMillis,
)
buildInfoPackage := "com.palficsabapeter.ck3.mapgenerator"

version := git.gitHeadCommit.value.getOrElse("no_info")
