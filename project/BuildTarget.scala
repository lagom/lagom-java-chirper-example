import sbt._

object BuildTarget {
  private sealed trait DeploymentRuntime
  private case object ConductR extends DeploymentRuntime
  private case object Kubernetes extends DeploymentRuntime

  private val deploymentRuntime: DeploymentRuntime = sys.props.get("buildTarget") match {
    case Some(v) if v.toLowerCase == "conductr" =>
      ConductR

    case Some(v) if v.toLowerCase == "kubernetes" =>
      Kubernetes

    case Some(v) =>
      sys.error(s"The build target $v is not supported. Only supports 'conductr' or 'kubernetes'")

    case None =>
      ConductR
  }

  val additionalLibraryDependencies: Seq[ModuleID] =
    if (deploymentRuntime == Kubernetes) Seq(Library.serviceLocatorDns) else Seq.empty

}