

plugins {
    `java-library`
    `maven-publish`
    id("net.neoforged.moddev")
    idea
   id("me.modmuss50.mod-publish-plugin")
}

val minecraft_version = property("minecraft_version") as String
val minecraft_version_range = property("minecraft_version_range") as String
val neo_version = property("neo_version") as String
val neo_version_range = property("neo_version_range") as String
val loader_version_range = property("loader_version_range") as String
val mod_id = property("mod_id") as String
val mod_group_id = property("mod_group_id") as String
val mod_name = property("mod_name") as String
val mod_license = property("mod_license") as String
val mod_version = property("mod_version") as String
val mod_authors = property("mod_authors") as String
val mod_description = property("mod_description") as String

val mainSourceSet = sourceSets.main.get()

version = "$mod_version-neoforge-$minecraft_version"
group = mod_group_id

base {
    archivesName = mod_id
}

java.toolchain.languageVersion = JavaLanguageVersion.of(21)

neoForge {
    version = neo_version

    parchment {
        mappingsVersion = property("parchment_mappings_version") as String
        minecraftVersion = property("parchment_minecraft_version") as String
    }

    runs {
        create("client") {
            client()
            gameDirectory = file("run/client")
            programArguments = listOf(
                "--username", "steve"
            )
            systemProperty("neoforge.enabledGameTestNamespaces", mod_id)
        }

        create("server") {
            server()
            gameDirectory = file("run/server")
            programArgument("--nogui")
            systemProperty("neoforge.enabledGameTestNamespaces", mod_id)
        }

        create("gameTestServer") {
            type = "gameTestServer"
            systemProperty("neoforge.enabledGameTestNamespaces", mod_id)
        }

        create("data") {
            data()
            gameDirectory = file("run/data")
            programArguments.addAll(
                "--mod", mod_id, "--all",
                "--output", file("src/generated/resources/").absolutePath,
                "--existing", file("src/main/resources/").absolutePath
            )
        }

        configureEach {
            jvmArguments = listOf(
                "-XX:+IgnoreUnrecognizedVMOptions",
                "-XX:+AllowEnhancedClassRedefinition"
            )
            systemProperty("forge.logging.markers", "REGISTRIES")
            logLevel = org.slf4j.event.Level.DEBUG
        }
    }

    mods {
        create(mod_id) {
            sourceSet(mainSourceSet)
        }
    }
}

mainSourceSet.resources.srcDir("src/generated/resources")

// Kotlin DSL 无 Groovy 的 methodMissing 自动创建：localRuntime 需显式创建
val localRuntimeConfig = configurations.create("localRuntime")
configurations.named("runtimeClasspath") {
    extendsFrom(localRuntimeConfig)
}

repositories {
    maven {
        name = "Modrinth"
        url = uri("https://api.modrinth.com/maven")
    }
}
dependencies {
    implementation("maven.modrinth:touhou-little-maid:1.4.0-neoforge+mc1.21.1")

    implementation("maven.modrinth:jade:15.9.1+neoforge")
    runtimeOnly("maven.modrinth:l_enders-cataclysm:3.16")
    runtimeOnly("maven.modrinth:lionfish-api:2.6")
    runtimeOnly("maven.modrinth:curios:9.5.1+1.21.1")
    runtimeOnly("maven.modrinth:repurposed-structures-forge:7.5.18+1.21.1-neoforge")
}

val generateModMetadata = tasks.register<ProcessResources>("generateModMetadata") {
    val replaceProperties = mapOf(
        "minecraft_version" to minecraft_version,
        "minecraft_version_range" to minecraft_version_range,
        "neo_version" to neo_version,
        "neo_version_range" to neo_version_range,

        "loader_version_range" to loader_version_range,

        "mod_id" to mod_id,
        "mod_name" to mod_name,
        "mod_license" to mod_license,
        "mod_version" to mod_version,
        "mod_authors" to mod_authors,
        "mod_description" to mod_description
    )
    inputs.properties(replaceProperties)
    expand(replaceProperties)
    from("src/main/templates")
    into("build/generated/sources/modMetadata")
}
mainSourceSet.resources.srcDir(generateModMetadata)
neoForge.ideSyncTask(generateModMetadata)

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
    repositories {
        maven {
            url = uri(file("repo"))
        }
    }
}

publishMods {
    file.set(tasks.named<Jar>("jar").flatMap { it.archiveFile })

    type.set( STABLE)
    modLoaders.add("neoforge")
    changelog.set("")
    curseforge {
        projectId.set("1255343")
        accessToken.set(providers.environmentVariable("CURSEFORGE_TOKEN"))
        minecraftVersions.add("1.21.1")
        client.set(true)
        server.set(true)

//        requires("fabric-api")
    }
    modrinth {
        projectId.set("euYoe1uE")
        accessToken.set(providers.environmentVariable("MODRINTH_TOKEN"))
        minecraftVersions.add("1.21.1")
    }

}
val copyParentLicense = tasks.register<Copy>("copyParentLicense") {
    from(rootProject.file("LICENSE"))
    into(layout.buildDirectory.dir("resources/main"))
}

// processResources depends on the license copy task
afterEvaluate {
    tasks.named("processResources") {
        dependsOn(copyParentLicense)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

idea {
    module {
        isDownloadSources = true
        isDownloadJavadoc = true
    }
}
