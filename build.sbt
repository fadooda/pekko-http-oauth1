import sbt.Keys.publishTo

val PROJECT_HOMEPAGE_URL = "https://github.com/dafutils/pekko-http-oauth1"
val PEKKO_HTTP_VERSION = "10.0.10"
val SIGNPOST_VERSION = "1.2.1.2"
val BINTRAY_USER = System.getenv("BINTRAY_USER")
val BINTRAY_PASSWORD = System.getenv("BINTRAY_PASS")
val PEKKO_VERSION = "2.5.3"

lazy val versionSettings = Seq(
  //  The 'version' setting is not set on purpose: its value is generated automatically by the sbt-dynver plugin
  //  based on the git tag/sha. Here we're just tacking on the maven-compatible snapshot suffix if needed
  version := {
    val snapshotVersion = dynverGitDescribeOutput.value
      .filter(gitVersion => gitVersion.isSnapshot())
      .map(output => output.version + "-SNAPSHOT")

    snapshotVersion.getOrElse(version.value)
  }
)

lazy val publicationSettings = Seq(
  publishTo := {
    val currentPublishTo =  publishTo.value
    if (isSnapshot.value)
        Some(s"Artifactory Realm" at "https://oss.jfrog.org/artifactory/oss-snapshot-local;build.timestamp=" + new java.util.Date().getTime)
    else
        currentPublishTo
  },
  credentials := Def.taskDyn{
      if (isSnapshot.value) {
        Def.task {
          Seq(
            Credentials(
              realm = "Artifactory Realm",
              host = "oss.jfrog.org",
              userName = BINTRAY_USER,
              passwd = BINTRAY_PASSWORD
            )
          )
        }
      }
      else
        Def.task { credentials.value }
  }.value,
  publishArtifact in Test := false,
  bintrayReleaseOnPublish := !isSnapshot.value
)


lazy val projectMetadataSettings = Seq(
  licenses += "Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.html"),
  homepage := Some(url(PROJECT_HOMEPAGE_URL)),
  scmInfo := Some(
    ScmInfo(
      browseUrl = url(PROJECT_HOMEPAGE_URL),
      connection = "scm:git:git@github.com:dafutils/pekko-http-oauth1.git"
    )
  ),
  developers := List(
    Developer(
      id = "edafinov",
      name = "Emil Dafinov",
      email = "emil.dafinov@gmail.com",
      url = url("https://github.com/dafutils")
    )
  )
)

lazy val pekkohttpoauth1 = (project in file("."))
  .settings(projectMetadataSettings)
  .settings(versionSettings)
  .settings(publicationSettings)
  .settings(
    scalaVersion := "2.12.7",

    organization := "com.github.dafutils",

    name := "pekko-http-oauth1",

    libraryDependencies ++= Seq(
      //Logging
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2",

      //Authentication
      "oauth.signpost" % "signpost-core" % SIGNPOST_VERSION,
      "oauth.signpost" % "signpost-commonshttp4" % SIGNPOST_VERSION,

      "org.apache.pekko" %% "pekko-http" % PEKKO_HTTP_VERSION % "provided",
      "org.apache.pekko" %% "pekko-http-testkit" % PEKKO_HTTP_VERSION % "test",

      //Test
      "org.scalatest" %% "scalatest" % "3.0.4" % "test",
      "org.mockito" % "mockito-all" % "1.10.19" % "test"
    )
  )
