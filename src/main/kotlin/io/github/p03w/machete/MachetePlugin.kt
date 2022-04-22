@file:Suppress("unused")

package io.github.p03w.machete

import io.github.p03w.machete.core.OxipngManager
import io.github.p03w.machete.tasks.DumpTasksWithOutputJarsTask
import io.github.p03w.machete.tasks.OptimizeJarsTask
import io.github.p03w.machete.util.knownGoodTasks
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

class MachetePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create("machete", MachetePluginExtension::class.java)

        val buildDir = File(project.buildDir.absolutePath).resolve("machete-build")
        buildDir.deleteRecursively()
        buildDir.mkdirs()

        OxipngManager.tempDir = buildDir
        OxipngManager.unpackOxipng()


        val optimizeTask = project.tasks.register("optimizeOutputJars", OptimizeJarsTask::class.java) { task ->
            var jarsToOptimize = mutableSetOf<File>()
            val tasksToCheck = knownGoodTasks.toMutableSet()
            extension.additionalTasks.orNull?.let {
                tasksToCheck.addAll(it)
            }
            extension.ignoredTasks.orNull?.let {
                tasksToCheck.removeAll(it)
            }

            tasksToCheck.forEach {
                val found = project.tasks.findByName(it)
                if (found != null) {
                    jarsToOptimize.addAll(found.outputs.files)
                }
            }

            extension.additionalJars.orNull?.let { additional ->
                jarsToOptimize.addAll(additional.map { File("${project.buildDir.absolutePath}/libs/$it") })
            }
            extension.ignoredJars.orNull?.let { ignored ->
                jarsToOptimize = jarsToOptimize.subtract(
                    ignored.mapTo(mutableSetOf()) { File("${project.buildDir.absolutePath}/libs/$it") }
                ).toMutableSet()
            }

            task.inputs.files(jarsToOptimize)
            if (extension.keepOriginal.get().not()) {
                task.outputs.files(jarsToOptimize)
            } else {
                task.outputs.files(jarsToOptimize.map {
                    it.resolveSibling(it.nameWithoutExtension + "-optimized.jar")
                })
            }

            task.buildDir.set(buildDir.absolutePath)
            task.extension.set(extension)
        }

        project.plugins.withId("java-base") {
            val buildTask = project.tasks.getByName("build")
            buildTask.finalizedBy(optimizeTask)
        }

        project.tasks.register("dumpTasksWithOutputJars", DumpTasksWithOutputJarsTask::class.java)
    }
}
