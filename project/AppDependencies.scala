import sbt.*

object AppDependencies {

  private val bootstrapVersion = "9.7.0"
  

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% "bootstrap-backend-play-30"  % bootstrapVersion,
    "com.beachape"            %% "enumeratum-play"            % "1.8.2"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-30"     % bootstrapVersion            % Test,
    
  )

  val it: Seq[Nothing] = Seq.empty
}
