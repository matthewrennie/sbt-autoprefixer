sbt-autoprefixer
================

[sbt-web](https://github.com/sbt/sbt-web) plugin that uses [Autoprefixer](https://github.com/ai/autoprefixer) to post-process CSS and add vendor prefixes to rules by [Can I Use](http://caniuse.com).

To use the latest version from Github, add the following to the `project/plugins.sbt` of your project:

```scala
    lazy val root = project.in(file(".")).dependsOn(sbtAutoprefixer)
    lazy val sbtAutoprefixer = uri("git://github.com/matthewrennie/sbt-autoprefixer")
```

Your project's build file also needs to enable sbt-web plugins. For example with build.sbt:

```scala
    lazy val root = (project in file(".")).enablePlugins(SbtWeb)
```

Declare the execution order of the asset pipeline:
```scala
pipelineStages in Assets := Seq(autoprefixer)
```

The following option are supported:

Option              | Description
--------------------|------------
cascade           	| Creates nice visual cascade of prefixes. Default: true.
sourceMap           | Enables source maps. Default: true.
inlineSourceMap     | Enables inline source maps by data:uri to annotation comment. Default: false.
    
The following sbt code illustrates how to include inline source maps 

```scala
AutoprefixerKeys.inlineSourceMap in Assets := true
```

To include all CSS files for post processing

```scala
includeFilter in autoprefixer := GlobFilter("*.css"),
```

If you receive duplicate map errors when using incombination with LESS, turn sourceMapping off
