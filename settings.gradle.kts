pluginManagement {
  repositories {
       maven {
      name = "Fabric"
      url = uri("https://maven.fabricmc.net/")
    }

maven("https://maven.parchmentmc.org")
    mavenCentral()
    gradlePluginPortal()
  }
}

dependencyResolutionManagement {
//  repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)

}

rootProject.name="pet-home-root"

include(":pet-home")


