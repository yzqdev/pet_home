pluginManagement {
  repositories {
    maven("https://maven.neoforged.net/releases")
    mavenCentral()
    gradlePluginPortal()
  }
}

dependencyResolutionManagement {
//  repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
 val versionToml=if (System.getenv("mc_proj")==null ) "../gradle/1_21.versions.toml" else System.getenv("mc_proj") + "\\1_21.versions.toml"
  versionCatalogs {
    create("libs") {
      from(files(versionToml))
    }
  }
}



include(":pet-home")


