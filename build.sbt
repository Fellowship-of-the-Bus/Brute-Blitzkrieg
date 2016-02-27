name := "Brute-Blitzkrieg"

import android.Keys._
android.Plugin.androidBuild

val androidVersionCode = Some(1)

exportJars := true
version := "0.1." + androidVersionCode
versionCode := androidVersionCode
updateCheck in Android := {} // disable update check
unmanagedClasspath in Test ++= (bootClasspath in Android).value
proguardScala in Android := true
useProguard in Android := true

packagingOptions in Android := PackagingOptions(
  pickFirsts = Seq("META-INF/NOTICE", "META-INF/LICENSE")
)

platformTarget := "android-16"

javacOptions ++= Seq(
  "-encoding", "utf8",
  "-source", "1.7",
  "-target", "1.7",
  "-Xlint"
)
scalaVersion := "2.11.7"
scalacOptions in Compile ++= Seq(
  "-unchecked",
  "-deprecation",
  "-feature",
  "-encoding", "utf8",
  "-target:jvm-1.7",
  "-optimize",
  "-Xlint",
  "-Yinline-warnings",
  "-Yinline",
  "-Yinline-handlers",
  "-Ybackend:GenBCode", // until 2.12 to eliminate inline warnings
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-value-discard",
  "-Ywarn-unused"
)

resolvers ++= Seq(
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
  Resolver.url(
    "sbt-plugin-releases",
    new URL("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases/")
  )(Resolver.ivyStylePatterns)
)

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.2.6" % "test",
  "junit" % "junit" % "4.12" % "test",
  "com.propensive" %% "rapture-json-jackson" % "2.0.0-M5",
  // "com.github.pathikrit" %% "better-files" % "2.14.0",
  // "com.github.fellowship_of_the_bus" %% "fellowship-of-the-bus-lib" % "0.3-SNAPSHOT" changing()

  // Tests //////////////////////////////
  "org.scaloid" %% "scaloid" % "4.1",
  "org.apache.maven" % "maven-ant-tasks" % "2.1.3" % "test",
  "org.robolectric" % "robolectric" % "3.0" % "test",
  "com.novocode" % "junit-interface" % "0.11" % "test"
)

updateCheck in Android := {} // disable update check
proguardCache in Android ++= Seq("org.scaloid")

proguardOptions in Android ++= Seq(
  "-dontobfuscate",
  "-dontoptimize",
  "-keepattributes Signature",
  "-printseeds target/seeds.txt",
  "-printusage target/usage.txt",
  "-dontwarn scala.collection.**", // required from Scala 2.11.4
  // "-dontwarn org.scaloid.**" // this can be omitted if current Android Build target is android-16
  "-keepattributes InnerClasses,EnclosingMethod",
  "-printconfiguration target/configuration.txt",
  "-dontwarn org.w3c.dom.bootstrap.DOMImplementationRegistry",
  "-dontwarn scala.xml.parsing.MarkupParser"
)

run <<= run in Android
install <<= install in Android

// without this, @Config throws an exception,
unmanagedClasspath in Test ++= (bootClasspath in Android).value
