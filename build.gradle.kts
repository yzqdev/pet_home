plugins {
    id("idea")
    id("net.neoforged.moddev") version "2.0.147" apply (false)
}

allprojects {
    repositories {
      

        maven {
            name = "Ladysnake"
            url = uri("https://maven.ladysnake.org/releases/")
        }

        maven {
            name = "ParchmentMC"
            url = uri("https://maven.parchmentmc.org")
        }

        exclusiveContent {
            forRepository {
                maven {
                    name = "Modrinth"
                    url = uri("https://api.modrinth.com/maven")
                }
            }

            filter {
                includeGroup("maven.modrinth")
            }
        }
    }
}