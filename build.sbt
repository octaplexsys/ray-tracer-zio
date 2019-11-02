import Dependencies._

enablePlugins(JmhPlugin)
enablePlugins(ScalafmtPlugin)

lazy val tpolecatSettings = Seq(
  scalacOptions ++=
    Seq(
      "-deprecation",                      // Emit warning and location for usages of deprecated APIs.
      "-encoding", "utf-8",                // Specify character encoding used by source files.
      "-explaintypes",                     // Explain type errors in more detail.
      "-feature",                          // Emit warning and location for usages of features that should be imported explicitly.
      "-language:existentials",            // Existential types (besides wildcard types) can be written and inferred
      "-language:higherKinds",             // Allow higher-kinded types
      "-language:implicitConversions",     // Allow definition of implicit functions called views
      "-language:postfixOps",     // Allow definition of implicit functions called views
      "-unchecked",                        // Enable additional warnings where generated code depends on assumptions.
      "-Xcheckinit",                       // Wrap field accessors to throw an exception on uninitialized access.
      //      "-Xfatal-warnings",                  // Fail the compilation if there are any warnings.
      "-Xfuture",                          // Turn on future language features.
      "-Xlint:adapted-args",               // Warn if an argument list is modified to match the receiver.
      "-Xlint:by-name-right-associative",  // By-name parameter of right associative operator.
      "-Xlint:constant",                   // Evaluation of a constant arithmetic expression results in an error.
      "-Xlint:delayedinit-select",         // Selecting member of DelayedInit.
      "-Xlint:doc-detached",               // A Scaladoc comment appears to be detached from its element.
      "-Xlint:inaccessible",               // Warn about inaccessible types in method signatures.
      "-Xlint:infer-any",                  // Warn when a type argument is inferred to be `Any`.
      "-Xlint:missing-interpolator",       // A string literal appears to be missing an interpolator id.
      "-Xlint:nullary-override",           // Warn when non-nullary `def f()' overrides nullary `def f'.
      "-Xlint:nullary-unit",               // Warn when nullary methods return Unit.
      "-Xlint:option-implicit",            // Option.apply used implicit view.
      "-Xlint:package-object-classes",     // Class or object defined in package object.
      "-Xlint:poly-implicit-overload",     // Parameterized overloaded implicit methods are not visible as view bounds.
      "-Xlint:private-shadow",             // A private field (or class parameter) shadows a superclass field.
      "-Xlint:stars-align",                // Pattern sequence wildcard must align with sequence component.
      "-Xlint:type-parameter-shadow",      // A local type parameter shadows a type already in scope.
      "-Xlint:unsound-match",              // Pattern match may not be typesafe.
      "-Yno-adapted-args",                 // Do not adapt an argument list (either by inserting () or creating a tuple) to match the receiver.
      // "-Yno-imports",                      // No predef or default imports
      "-Ypartial-unification",             // Enable partial unification in type constructor inference
      "-Ywarn-dead-code",                  // Warn when dead code is identified.
      "-Ywarn-extra-implicit",             // Warn when more than one implicit parameter section is defined.
      "-Ywarn-inaccessible",               // Warn about inaccessible types in method signatures.
      "-Ywarn-infer-any",                  // Warn when a type argument is inferred to be `Any`.
      "-Ywarn-nullary-override",           // Warn when non-nullary `def f()' overrides nullary `def f'.
      "-Ywarn-nullary-unit",               // Warn when nullary methods return Unit.
      "-Ywarn-numeric-widen",              // Warn when numerics are widened.
      //      "-Ywarn-unused:implicits",           // Warn if an implicit parameter is unused.
      //      "-Ywarn-unused:imports",             // Warn if an import selector is not referenced.
      //      "-Ywarn-unused:locals",              // Warn if a local definition is unused.
      //      "-Ywarn-unused:params",              // Warn if a value parameter is unused.
      // "-Ywarn-unused:patvars",             // Warn if a variable bound in a pattern is unused.
      "-Ywarn-unused:privates",            // Warn if a private member is unused.
      "-Ywarn-value-discard",              // Warn when non-Unit expression results are unused.
      "-Ywarn-macros:before", // via som
      "-Yrangepos" // for longer squiggles
      /*plus JVM options -XX:MaxInlineLevel=18 -XX:MaxInlineSize=270 -XX:MaxTrivialSize=12 (https://twitter.com/leifwickland/status/1179419045055086595)*/
    )
  ,
  scalacOptions in (Compile, console) --= Seq("-Xfatal-warnings", "-Ywarn-unused:imports", "-Yno-imports")
)

lazy val commonSettings = inThisBuild(

  tpolecatSettings ++
    Seq(
      scalaVersion := "2.12.10",
      addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.4"),
      addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.0-M4"),
      addCompilerPlugin(("org.scalamacros" % "paradise"  % "2.1.1") cross CrossVersion.full),
      testFrameworks ++= Seq(new TestFramework("zio.test.sbt.ZTestFramework")),
      libraryDependencies ++= Seq(
        zioTest % "test",
        zioTestSbt % "test"
      )
    )
)


lazy val `simple-http4s` = project
  .in(file("simple-http4s"))
  .settings(commonSettings)
  .settings(
      name := "simple-http4s",
      libraryDependencies ++= Seq(
        zio,
        zioCats exclude("dev.zio", "zio-test"),
        cats,
        catsEffect,
        http4sServer,
        http4sDsl,
        http4sCirce,
        circeCore,
        circeGeneric,
        circeGenericX,
        log4CatsSlf4j
      )
  )

lazy val geometry = project
  .in(file("geometry"))
  .settings(commonSettings)
  .settings(
    name := "geometry",
    libraryDependencies ++= Seq(
      zio,
      zioCats exclude("dev.zio", "zio-test"),
      cats,
      catsEffect,
      fs2,
      breeze,
      breezeNative,
      singletonOps,
      log4CatsSlf4j,
      scalaTest % "test",
      scalaCheck % "test"
    )
  )
  .dependsOn(commons)

lazy val commons = project
  .in(file("commons"))
  .settings(commonSettings)
  .settings(
    name := "commons",
    libraryDependencies ++= Seq(
      zio,
      zioStreams,
      cats,
      mouse
    )
  )

lazy val `ray-tracer` = project
  .in(file("ray-tracer"))
  .settings(commonSettings)
  .settings(
    name := "ray-tracer",
    libraryDependencies ++= Seq(
      zio,
      zioNio,
      zioCats exclude("dev.zio", "zio-test"),
      cats,
      catsEffect,
      log4CatsSlf4j,
      scalaTest % "test"
    )
  )
  .dependsOn(geometry % "test->test;compile->compile")
