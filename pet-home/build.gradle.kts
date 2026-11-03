import me.modmuss50.mpp.ReleaseType
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import sun.java2d.StateTrackable


plugins {
    id("net.fabricmc.fabric-loom-remap") version "1.18-SNAPSHOT"
    id("maven-publish")
    idea
    id("me.modmuss50.mod-publish-plugin")
}

val mod_version = property("mod_version") as String
val maven_group = property("maven_group") as String
val archives_base_name = property("archives_base_name") as String
val minecraft_version = property("minecraft_version") as String

val mainSourceSet = sourceSets.main.get()

version = "$mod_version-fabric-$minecraft_version"
group = maven_group

base {
    archivesName = archives_base_name
}
loom {
    accessWidenerPath = file("src/main/resources/pet_home.accesswidener")
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
        outputDirectory = file("src/generated/resources")
        client = true
    }
}

// 原写法依赖 groovy 动态派发到 runDatagen(JavaExec) 的 systemProperty；kts 需显式过滤类型
tasks.withType<JavaExec>().matching { it.name == "runDatagen" }.configureEach {
    systemProperty("fabric-api.datagen.modid", "pet_home")
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

mainSourceSet.resources.srcDir("src/generated/resources")

tasks.withType<AbstractArchiveTask>().configureEach {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.withType<Copy>().configureEach {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraft_version")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-1.21.1:2024.11.17@zip")
    })

    modImplementation("net.fabricmc:fabric-loader:0.18.4")
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.116.9+1.21.1")

    modImplementation("maven.modrinth:jade:15.10.6+fabric")
    // forge-config-api-port 已移除：pet-home 配置系统早已迁到自研 ConfigSpec（代码零引用 fuzs/nightconfig），
    // 该依赖只会在 dev 运行期注入 FCAP，而 FCAP 缺 Night Config → runServer 启动即 NoClassDefFoundError 崩溃
    modImplementation("com.terraformersmc:modmenu:11.0.3")

    // 可选联动（suggests）：geckolib 渲染器挂宠物 overlay 的 compat 层（compat/geckolib），
  // 与 26.1 侧同款：编译期可见即可，运行期由用户自行安装 geckolib。
  modCompileOnly("software.bernie.geckolib:geckolib-fabric-1.21.1:4.8.4")
  compileOnly("com.google.code.findbugs:jsr305:3.0.2")
}

// TLM 只做编译期（modCompileOnly）：mixin 目标类结构 + refmap 生成即可；运行期不带 TLM。
// 发布 jar 不依赖 TLM（EntityMaidMixin 由 PetHomeMixinPlugin 按 isModLoaded 守卫）。
// dev 冒烟要真 TLM 的，在 :foundation 侧自持挂载（含嵌套解压与 night-config）。
val tlmDep = "maven.modrinth:touhoulittlemaid-orihime:0.8.2-neo1.5.3+mc1.21.1"

dependencies {
    modCompileOnly(tlmDep)
}

tasks.processResources {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    val modVersion = mod_version
    inputs.property("version", modVersion)

    filesMatching("fabric.mod.json") {
        expand(mapOf("version" to modVersion))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(21)
    options.encoding = "UTF-8"
}

java {
    withSourcesJar()
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

idea {
    module {
        isDownloadSources = true
        isDownloadJavadoc = true
    }
}
publishMods {
    file.set(tasks.named("remapJar").flatMap { (it as org.gradle.jvm.tasks.Jar).archiveFile })

    type.set(ReleaseType.STABLE)
    modLoaders.add("fabric")
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