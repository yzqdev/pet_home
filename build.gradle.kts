plugins {
    id("idea")
    id("net.neoforged.moddev") version "2.0.80" apply (false)
}

allprojects {

    layout.buildDirectory.set(File(rootDir, "build/${path.replace(":", "/")}"))
}