package net.matthewrennie.sbt.autoprefixer

import sbt._
import sbt.Keys._
import com.typesafe.sbt.web.SbtWeb
import com.typesafe.sbt.web.pipeline.Pipeline
import com.typesafe.sbt.jse.{SbtJsEngine, SbtJsTask}

object Import {

  val autoprefixer = TaskKey[Pipeline.Stage]("autoprefixer", "Parse CSS and adds vendor prefixes to rules by Can I Use")

  object AutoprefixerKeys {
    val buildDir = SettingKey[File]("autoprefixer-build-dir", "Where autoprefixer will read from.")
    val cascade = SettingKey[Boolean]("autoprefixer-cascade", "Creates nice visual cascade of prefixes. The default is that cascade is enabled (true).")
    val inlineSourceMap = SettingKey[Boolean]("autoprefixer-inline-source-map", "Enables inline source maps by data:uri to annotation comment. The default is that inline source maps are dsiabled (false).")
    val sourceMap = SettingKey[Boolean]("autoprefixer-source-map", "Enables source maps. The default is that source maps are enabled (true).")
  }

}

object SbtAutoprefixer extends AutoPlugin {

  override def requires = SbtJsTask

  override def trigger = AllRequirements

  val autoImport = Import

  import SbtWeb.autoImport._
  import WebKeys._
  import SbtJsEngine.autoImport.JsEngineKeys._
  import SbtJsTask.autoImport.JsTaskKeys._
  import autoImport._
  import AutoprefixerKeys._

  override def projectSettings = Seq(
    buildDir := (resourceManaged in autoprefixer).value / "build",
    excludeFilter in autoprefixer := HiddenFileFilter,
    includeFilter in autoprefixer := GlobFilter("*.css"),
    resourceManaged in autoprefixer := webTarget.value / autoprefixer.key.label,
    cascade := true,
    sourceMap := true,
    inlineSourceMap := false,
    autoprefixer := runAutoprefixer.dependsOn(WebKeys.nodeModules in Assets).value
  )

  private def runAutoprefixer: Def.Initialize[Task[Pipeline.Stage]] = Def.task {
    mappings =>

      val include = (includeFilter in autoprefixer).value
      val exclude = (excludeFilter in autoprefixer).value
      val autoprefixerMappings = mappings.filter(f => !f._1.isDirectory && include.accept(f._1) && !exclude.accept(f._1))

      SbtWeb.syncMappings(
        streams.value.cacheDirectory,
        autoprefixerMappings,
        buildDir.value
      )

      val buildMappings = autoprefixerMappings.map(o => buildDir.value / o._2)

      val cacheDirectory = streams.value.cacheDirectory / autoprefixer.key.label
      val runUpdate = FileFunction.cached(cacheDirectory, FilesInfo.hash) {
        inputFiles =>
          streams.value.log.info("Autoprefixing CSS")

          val inputFileArgs = inputFiles.map(_.getPath)

          val cascadeArgs = if (cascade.value) Seq("--cascade") else Nil

          val sourceMapArgs = if (sourceMap.value) Seq("--map") else Nil

          val inlineSourceMapArgs = if (inlineSourceMap.value) Seq("--inline-map") else Nil

          val allArgs = Seq() ++ 
            inputFileArgs ++
            cascadeArgs ++
            sourceMapArgs ++
            inlineSourceMapArgs

          SbtJsTask.executeJs(
            state.value,
            (engineType in autoprefixer).value,
            (command in autoprefixer).value,
            (nodeModuleDirectories in Assets).value.map(_.getPath),            
            (nodeModuleDirectories in Assets).value.last / "autoprefixer" / "bin" / "autoprefixer",
            allArgs,
            (timeoutPerSource in autoprefixer).value * autoprefixerMappings.size
          )

          buildDir.value.***.get.filter(!_.isDirectory).toSet
      }

      val autoPrefixedMappings = runUpdate(buildMappings.toSet).pair(relativeTo(buildDir.value))
      (mappings.toSet -- autoprefixerMappings ++ autoPrefixedMappings).toSeq
  }

}