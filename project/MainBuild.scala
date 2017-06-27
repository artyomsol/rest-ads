import sbt.Build

object MainBuild extends Build {
  val VersionRegex = "v\\.?([0-9]+.[0-9]+.[0-9x+]+-?.*)?".r
  val customTagToVersionNumber: String => Option[String] = {
    //    git.defaultTagByVersionStrategy {
    case VersionRegex(v) => Some(v)
    case _ => None
  }
}
