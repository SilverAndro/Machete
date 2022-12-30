@file:Suppress("unused")

package io.github.p03w.machete

import io.github.p03w.machete.config.MachetePluginExtension
import io.github.p03w.machete.core.OxipngManager
import io.github.p03w.machete.patches.patches
import io.github.p03w.machete.tasks.DumpTasksWithOutputJarsTask
import io.github.p03w.machete.tasks.OptimizeJarsTask
import io.github.p03w.machete.util.capital
import io.github.p03w.machete.util.knownGoodTasks
import io.github.p03w.machete.util.resolveAndMakeDir
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

class MachetePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create("machete", MachetePluginExtension::class.java)

        val buildDir = File(project.buildDir.absolutePath).resolve("machete-build")
        buildDir.deleteRecursively()
        buildDir.mkdirs()

        OxipngManager.tempDir = project.rootProject.buildDir
        OxipngManager.unpackOxipng(project.name)

        project.afterEvaluate {
            if (extension.enabled.get().not()) {
                project.logger.lifecycle("Machete was disabled on this build through the `enabled` flag!")
                return@afterEvaluate
            }

            val tasksToCheck = knownGoodTasks.toMutableSet()

            extension.additionalTasks.orNull?.let {
                tasksToCheck.addAll(it)
            }
            extension.ignoredTasks.orNull?.let {
                tasksToCheck.removeAll(it)
            }

            project.logger.info("All tasks to check: $tasksToCheck")

            tasksToCheck.forEach { taskName ->
                val found = project.tasks.findByName(taskName)
                if (found != null) {
                    project.logger.info("Tasks $taskName exists! Generating a hook task")
                    val toOptimize = found.outputs.files
                    val optimizeTask = project.tasks.create(
                        "optimizeOutputsOf${taskName.capital()}",
                        OptimizeJarsTask::class.java
                    ) { optimizeTask ->
                        optimizeTask.group = "machete"
                        optimizeTask.description =
                            "An auto-generated task to optimize the output artifacts of $taskName"

                        // Try and set the inputs and outputs
                        optimizeTask.inputs.files(toOptimize)
                        if (extension.keepOriginal.get().not()) {
                            optimizeTask.outputs.files(toOptimize)
                        } else {
                            optimizeTask.outputs.files(toOptimize.map { file ->
                                file.resolveSibling(file.nameWithoutExtension + "-optimized.jar")
                            })
                        }
                        // We can cache if we arent replacing anything
                        // Gradle does handle this for us, but doesnt hurt to be explicit
                        optimizeTask.outputs.cacheIf { extension.keepOriginal.get() }

                        // Give everything its own sibling dir to prevent overlapping on parallel tasks
                        optimizeTask.buildDir.set(buildDir.resolveAndMakeDir(taskName).absolutePath)
                        optimizeTask.extension.set(extension)
                    }

                    // Hook after build
                    val after = extension.finalizeAfter.get()
                    if (after.isNotBlank()) {
                        project.tasks.getByName(after).finalizedBy(optimizeTask)
                    }
                    optimizeTask.dependsOn(found)
                }
            }

            // Try and apply any compatibility patches
            patches.forEach {
                project.logger.info("Checking if patch ${it::class.simpleName} should apply")
                if (it.shouldApply(project, extension)) {
                    project.logger.info("Applying project patch ${it::class.simpleName}")
                    it.patch(project, extension)
                }
            }
        }

        project.tasks.register("dumpTasksWithOutputJars", DumpTasksWithOutputJarsTask::class.java)
    }
}
