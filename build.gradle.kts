import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.20"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    java
    `maven-publish`
    `java-gradle-plugin`
}

group = "io.github.p03w"
version = "1.0.0"

repositories {
    mavenCentral()
}

buildscript {
    dependencies {
        classpath(kotlin("gradle-plugin"))
    }
}

dependencies {
    shadow("com.google.code.gson:gson:2.9.0")
    shadow("net.lingala.zip4j:zip4j:2.10.0")
}

tasks.withType<ShadowJar> {
    configurations = listOf(
        project.configurations.getByName("shadow")
    )

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

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "io.github.p03w.machete"
            artifactId = "machete"
            version = "1.0.0"

            from(project.components.getByName("java"))
            //artifact(tasks.shadowJar)
        }
    }
}

gradlePlugin {
    plugins {
        create("mixlinPlugin") {
            id = "io.github.p03w.machete"
            implementationClass = "io.github.p03w.machete.MachetePlugin"
        }
    }
}
