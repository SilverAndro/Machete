import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version "1.6.20"
    id("com.gradle.plugin-publish") version "1.0.0-rc-1"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "io.github.p03w"
version = "1.0.3"
description = "A gradle plugin to optimize built jars through individual file optimizations and increased compression"

//region Dependencies
repositories {
    mavenCentral()
}

dependencies {
    shadow("com.google.code.gson:gson:2.9.0")
    shadow("net.lingala.zip4j:zip4j:2.10.0")
}
//endregion

//region Task Configure
tasks.withType<ShadowJar> {
    configurations = listOf(
        project.configurations.getByName("shadow")
    )

    relocate("com.google", "shadow.google")
    relocate("net.lingala", "shadow.lingala")

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
    website = "https://github.com/P03W/Machete/"
    vcsUrl = "https://github.com/P03W/Machete/"
    description = project.description
    tags = listOf("jar", "build", "jvm", "compress")
}
//endregion
