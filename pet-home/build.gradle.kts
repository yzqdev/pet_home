import org.gradle.api.tasks.AbstractCopyTask
import me.modmuss50.mpp.ReleaseType.STABLE

plugins {
    id("net.fabricmc.fabric-loom-remap") version "1.17-SNAPSHOT"
    id("maven-publish")
    id("idea")
    id("me.modmuss50.mod-publish-plugin")
}

val mod_version = property("mod_version") as String
val minecraft_version = property("minecraft_version") as String
val maven_group = property("maven_group") as String
val archives_base_name = property("archives_base_name") as String

version = mod_version
group = maven_group

base {
    archivesName = archives_base_name
}
repositories {

}
loom {
    accessWidenerPath = file("src/main/resources/pet_home.accesswidener")
    runs {
        named("client") {
            jvmArguments.addAll("-XX:+AllowEnhancedClassRedefinition")
            programArguments.addAll("--username", "steve")
            client()
        }
    }
}

fabricApi {
    configureDataGeneration {
        outputDirectory = file("src/generated/resources")
    }
}

sourceSets.named("main") {
    resources.srcDir("src/generated/resources")
}

dependencies {
    minecraft("com.mojang:minecraft:${minecraft_version}")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-1.20.1:2023.09.03@zip")
    })

    modImplementation("net.fabricmc:fabric-loader:0.16.14")
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.92.5+1.20.1")
    modImplementation("maven.modrinth:jade:11.12.0+fabric")
    modImplementation("maven.modrinth:forge-config-api-port:v8.0.3-1.20.1-Fabric")
    modImplementation("com.terraformersmc:modmenu:7.2.2")
}


val tlmDep = "maven.modrinth:touhoulittlemaid-orihime:0.8.2-forge1.5.3+mc1.20.1"


// TLM 只做编译期（modCompileOnly）：mixin 目标类结构 + refmap 生成即可；运行期不带 TLM。
// 嵌套 jar（porting_lib / cardinal 等）的解压不再由本模块负责——:foundation 的冒烟运行需要时
// 在其自身构建脚本里解压到 pet-home/build/tlm-nested-jars。生产环境装了 TLM 时 mixin 自然生效。
dependencies {
    modCompileOnly(tlmDep)
    // GeckoLib 宠物 overlay 适配（compat/geckolib 三件套）：编译期需要；运行期为可选兼容，
    // 未装 GeckoLib 时 GeoOverlayCompat 降级不挂层（foundation 工作区经 jean 带上 geckolib）。
    modCompileOnly("software.bernie.geckolib:geckolib-fabric-1.20.1:4.7.1.2")
    modLocalRuntime("software.bernie.geckolib:geckolib-fabric-1.20.1:4.7.1.2")
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
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    val modVersion = project.version.toString()
    inputs.property("version", modVersion)

    filesMatching("fabric.mod.json") {
        expand(mapOf("version" to modVersion))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 17
    options.encoding = "UTF-8"
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.jar {
    val archiveName = base.archivesName.get()
    from("LICENSE") {
        rename { "${it}_$archiveName" }
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = archives_base_name
            from(components["java"])
        }
    }
    repositories {
    }
}


tasks.withType<AbstractCopyTask>().configureEach {
    exclude("**/.cache/**")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

publishMods {
    file.set(tasks.named("remapJar").flatMap { (it as org.gradle.jvm.tasks.Jar).archiveFile })

    type.set(STABLE)
    modLoaders.add("fabric")
    changelog.set("")
    curseforge {
        projectId.set("1255343")
        accessToken.set(providers.environmentVariable("CURSEFORGE_TOKEN"))
        minecraftVersions.add("1.20.1")
        client.set(true)
        server.set(true)

//        requires("fabric-api")
    }
    modrinth {
        projectId.set("euYoe1uE")
        accessToken.set(providers.environmentVariable("MODRINTH_TOKEN"))
        minecraftVersions.add("1.20.1")
    }

}