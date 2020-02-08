import sbt.Def._
import sbt.{Def, settingKey}

object BuildKeys {
  /**
    * When this value is set, it means we want to test and publish a custom Scala.js
    * version, therefore we shouldn't re-publish the JVM packages.
    */
  lazy val customScalaJSVersion =
    Option(System.getenv("SCALAJS_VERSION"))

  /**
    * Human readable project title.
    *
    * Examples:
    *
    *  - Cats
    *  - Cats Effect
    *  - Monix
    */
  lazy val projectTitle =
    settingKey[String]("Human readable project title (e.g. 'Cats Effect', 'Monix', etc)")

  /**
    * Project homepage root URL.
    *
    * Example: [[https://alexandru.github.io/]]
    */
  lazy val projectWebsiteRootURL =
    settingKey[String]("Project homepage full URL")

  /**
    * Project homepage root URL.
    *
    * Example: `/my-typelevel-library/` or `/`
    */
  lazy val projectWebsiteBasePath =
    settingKey[String]("Project homepage base path")

  /**
    * Full website URL.
    */
  lazy val projectWebsiteFullURL =
    Def.setting(
      s"${projectWebsiteRootURL.value.replaceAll("[/]+$", "")}/${projectWebsiteBasePath.value.replaceAll("^[/]+", "")}"
    )

  /**
    * Example: alexandru, monix, typelevel, etc.
    */
  lazy val githubOwnerID =
    settingKey[String]("GitHub owner ID (e.g. user_id, organization_id)")

  /**
    * Example: alexandru, monix, typelevel, etc.
    */
  lazy val githubRelativeRepositoryID =
    settingKey[String]("GitHub repository ID (e.g. project_name)")

  /**
    * Example: `alexandru/my-typelevel-library`
    */
  lazy val githubFullRepositoryID =
    Def.setting(
      s"${githubOwnerID.value}/${githubOwnerID.value}"
    )

  /**
    * Folder where the API docs will be uploaded when generating site.
    *
    * Typically: "api"
    */
  lazy val docsMappingsAPIDir =
    settingKey[String]("Name of subdirectory in site target directory for api docs")

  /**
    * Auto-detected by the build process.
    */
  lazy val needsScalaMacroParadise =
    settingKey[Boolean]("Needs Scala Macro Paradise")        
}
