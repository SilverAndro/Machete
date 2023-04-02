package io.github.p03w.machete.tasks

import io.github.p03w.machete.core.OxipngManager
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class UnpackOxipngTask : DefaultTask() {
    @TaskAction
    fun unpackOxipng() {
        val buildDir = File(project.buildDir.absolutePath).resolve("machete-build")
        buildDir.mkdirs()

        OxipngManager.tempDir = buildDir
        OxipngManager.unpackOxipng(project.name)
    }
}