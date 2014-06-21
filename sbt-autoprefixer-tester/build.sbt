import AutoprefixerKeys._
import WebJs._

lazy val root = (project in file(".")).enablePlugins(SbtWeb)

JsEngineKeys.engineType := JsEngineKeys.EngineType.Node

pipelineStages := Seq(autoprefixer)
