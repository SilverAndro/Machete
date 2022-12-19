import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version "1.6.20"
    id("com.gradle.plugin-publish") version "1.0.0-rc-1"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "io.github.p03w"
version = "1.2.0"
description = "A gradle plugin to optimize built jars through individual file optimizations and increased compression, works best on resource heavy jars"

//region Dependencies
repositories {
    mavenCentral()
}

dependencies {
    val asmVer = "9.3"
    shadow("org.ow2.asm:asm:$asmVer")
    shadow("org.ow2.asm:asm-tree:$asmVer")
    shadow("org.ow2.asm:asm-commons:$asmVer")
}
//endregion

//region Task Configure
tasks.withType<ShadowJar> {
    configurations = listOf(
        project.configurations.getByName("shadow")
    )

    relocate("org.ow2.asm", "s_m.ow2.asm")

    minimize()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.freeCompilerArgs = listOf("-opt-in=kotlin.RequiresOptIn")
}

tasks.withType<GenerateModuleMetadata> {
    enabled = false
}

tasks.withType<ShadowJar> {
    archiveBaseName.set("machete")
    archiveClassifier.set("")
    archiveVersion.set(project.version.toString())
}
//endregion

//region Plugin Configure
gradlePlugin {
    plugins {
        create("machetePlugin") {
            id = "io.github.p03w.machete"
            displayName = "Machete"
            implementationClass = "io.github.p03w.machete.MachetePlugin"
        }
    }
}

pluginBundle {
    website = "https://github.com/SilverAndro/Machete"
    vcsUrl = "https://github.com/SilverAndro/Machete"
    description = project.description
    tags = listOf("jar", "build", "jvm", "compress")
}
//endregion
