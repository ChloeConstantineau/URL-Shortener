import org.typelevel.sbt.tpolecat.{CiMode, DevMode}
import org.typelevel.scalacoptions.ScalacOptions

// *****************************************************************************
// Global settings
// *****************************************************************************

Global / onChangedBuildSource := ReloadOnSourceChanges

ThisBuild / version             := "1.0.0"
ThisBuild / scalaVersion        := "3.5.2"
ThisBuild / scalacOptions ++= Seq("-deprecation", "-unchecked")
ThisBuild / semanticdbEnabled   := true // For scalafix
ThisBuild / semanticdbVersion   := scalafixSemanticdb.revision
ThisBuild / tpolecatDevModeOptions ++= additionalScalacOptions
ThisBuild / tpolecatOptionsMode := {
  if (insideCI.value) CiMode else DevMode
}

addCommandAlias("lint", "scalafmtSbt; scalafmtAll; scalafixAll")
addCommandAlias(
  "lintCheck",
  "scalafmtSbtCheck; scalafmtCheckAll; scalafixAll --check",
)

// *****************************************************************************
// Project
// *****************************************************************************

lazy val root = project
  .in(file("."))
  .settings(
    name       := "URL-Shortener",
    run / fork := true,
    libraryDependencies ++= dependencies,
  )

lazy val dependencies = Seq(
  "dev.zio" %% "zio"                 % "2.1.14",
  "dev.zio" %% "zio-config"          % "4.0.3",
  "dev.zio" %% "zio-config-magnolia" % "4.0.3",
  "dev.zio" %% "zio-config-typesafe" % "4.0.3",
  "dev.zio" %% "zio-http"            % "3.0.1",
  "dev.zio" %% "zio-json"            % "0.7.4",
  "dev.zio" %% "zio-redis"           % "1.0.0",
  "dev.zio" %% "zio-test"            % "2.1.14" % Test,
  "dev.zio" %% "zio-test-magnolia"   % "2.1.14" % Test,
  "dev.zio" %% "zio-test-sbt"        % "2.1.14" % Test,
)

// *****************************************************************************
// Options
// *****************************************************************************

lazy val additionalScalacOptions = Set(
  ScalacOptions.source3,
  ScalacOptions.verboseTypeDiffs,
  ScalacOptions.verboseImplicits,
  // With these two flags, we enforce optional braces around template bodies and method arguments. scalafmt `rewrite.scala3.removeOptionalBraces` isn't enough.
  // See: https://dotty.epfl.ch/docs/reference/other-new-features/indentation.html#settings-and-rewrites-1
  ScalacOptions.other("-rewrite", _ => true),
  ScalacOptions.other("-indent", _ => true),
)
