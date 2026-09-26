pluginManagement {
    repositories {
        // maven {
        //     name = "MinecraftForge"
        //     url = uri("https://maven.minecraftforge.net/")
        // }

        maven {
            url = uri("https://maven.parchmentmc.org")
        }

        maven {
            url = uri("https://repo.spongepowered.org/repository/maven-public/")
        }

        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    // repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
}

rootProject.name = "pet-home-root"

include(":pet-home")