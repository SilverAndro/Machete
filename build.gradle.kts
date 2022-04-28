import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version "1.6.20"
    id("com.gradle.plugin-publish") version "1.0.0-rc-1"
}

group = "io.github.p03w"
version = "1.0.8"
description = "A gradle plugin to optimize built jars through individual file optimizations and increased compression"

//region Dependencies
repositories {
    mavenCentral()
}

dependencies {

}
//endregion

//region Task Configure
tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.freeCompilerArgs = listOf("-opt-in=kotlin.RequiresOptIn")
}

tasks.withType<GenerateModuleMetadata> {
    enabled = false
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
