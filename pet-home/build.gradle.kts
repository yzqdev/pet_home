plugins {
	id("net.fabricmc.fabric-loom")
	id("maven-publish")
	id("me.modmuss50.mod-publish-plugin")
}


val mod_version = property("mod_version") as String
val minecraft_version = property("minecraft_version") as String
val loader_version = property("loader_version") as String
val fabric_api_version = property("fabric_api_version") as String
val maven_group = property("maven_group") as String
val archives_base_name = property("archives_base_name") as String

version = "$mod_version-fabric-$minecraft_version"
group = maven_group

base {
	archivesName.set(archives_base_name)
}




loom {
	accessWidenerPath = file("src/main/resources/pet_home.classtweaker")
	runs {
		named("client") {
			jvmArguments = listOf("-XX:+AllowEnhancedClassRedefinition")
			programArguments = listOf("--username", "steve")
			client()
		}
	}


}

fabricApi {
	configureDataGeneration {
		client = true
	}
}


sourceSets {
	main {
		resources {
			setSrcDirs(listOf("src/main/resources", "src/main/generated"))
		}
	}
}

dependencies {
	// To change the versions see the gradle.properties file
	minecraft("com.mojang:minecraft:$minecraft_version")

	implementation("net.fabricmc:fabric-loader:$loader_version")

	// Fabric API. This is technically optional, but you probably want it anyway.
	implementation("net.fabricmc.fabric-api:fabric-api:$fabric_api_version")
	implementation("maven.modrinth:jade:26.1.11+fabric")

	implementation("com.terraformersmc:modmenu:18.0.1")
	// 可选联动（suggests）：geckolib 渲染器挂宠物 overlay 的 compat 层（compat/geckolib），
	// 仅编译期需要（运行时由 lynae/skirk 等模组传递提供，FabricLoader.isModLoaded 门控）
	compileOnly("com.geckolib:geckolib-fabric-26.1.2:5.5.2")
	compileOnly("org.projectlombok:lombok:1.18.46")
	annotationProcessor("org.projectlombok:lombok:1.18.46")
}

val copyParentLicense = tasks.register<Copy>("copyParentLicense") {
	from(rootProject.file("LICENSE"))
	into(layout.buildDirectory.dir("resources/main"))
}


afterEvaluate {
	tasks.named("processResources") {
		dependsOn(copyParentLicense)
	}
}

tasks.processResources {
	inputs.property("version", project.version)

	filesMatching("fabric.mod.json") {
		expand(mapOf("version" to project.version))
	}
}

tasks.withType<JavaCompile>().configureEach {
	options.release.set(25)
}

java {
	// Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
	// if it is present.
	// If you remove this line, sources will not be generated.
	withSourcesJar()

	sourceCompatibility = JavaVersion.VERSION_25
	targetCompatibility = JavaVersion.VERSION_25
}

tasks.jar {
	inputs.property("archivesName", project.base.archivesName)

	from("LICENSE") {
		rename { "${it}_${inputs.properties["archivesName"]}" }
	}
}

// configure the maven publication
publishing {
	publications {
		create<MavenPublication>("mavenJava") {
			artifactId = archives_base_name
			from(components["java"])
		}
	}

	// See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
	repositories {
		// Add repositories to publish to here.
		// Notice: This block does NOT have the same function as the block in the top level.
		// The repositories here will be used for publishing your artifact, not for
		// retrieving dependencies.
	}
}

publishMods {
	file.set(tasks.named<Jar>("jar").flatMap { it.archiveFile })

	type.set( STABLE)
	modLoaders.add("fabric")
	changelog.set("")
	curseforge {
		projectId.set("1255343")
		accessToken.set(providers.environmentVariable("CURSEFORGE_TOKEN"))
		minecraftVersions.add("26.1.2")
		client.set(true)
		server.set(true)

//        requires("fabric-api")
	}
	modrinth {
		projectId.set("euYoe1uE")
		accessToken.set(providers.environmentVariable("MODRINTH_TOKEN"))
		minecraftVersions.add("26.1.2")
	}

}