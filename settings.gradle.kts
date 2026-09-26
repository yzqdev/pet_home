pluginManagement {
  repositories {
    maven("https://maven.neoforged.net/releases")
    mavenCentral()
    gradlePluginPortal()
  }
}

dependencyResolutionManagement {
//  repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)

}

rootProject.name="pet-home-root"

include(":pet-home")


