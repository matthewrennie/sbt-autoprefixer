lazy val root = (project in file(".")).enablePlugins(SbtWeb)

JsEngineKeys.engineType := JsEngineKeys.EngineType.Node

pipelineStages := Seq(autoprefixer)

val checkCSSFileContents = taskKey[Unit]("check that css contents are correct")

checkCSSFileContents := {
  val contents = IO.read(file("target/web/stage/css/test.css"))
  if (!contents.contains("-webkit-transition")) {
    sys.error(s"Unexpected contents: $contents")
  }
}

val checkSourceMapFileContents = taskKey[Unit]("check that source map contents are correct")

checkSourceMapFileContents := {
  val contents = IO.read(file("target/web/stage/css/test.css.map"))
  if (!contents.contains("test.css")) {
    sys.error(s"Unexpected contents: $contents")
  }
}