plugins {
    id("idea")
    id("net.neoforged.moddev.legacyforge") version "2.0.141" apply (false)
   id("me.modmuss50.mod-publish-plugin") version "2.2.1" apply(false)
}

allprojects {
    repositories {
      

   
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