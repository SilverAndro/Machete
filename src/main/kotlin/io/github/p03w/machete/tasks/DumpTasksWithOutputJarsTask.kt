package io.github.p03w.machete.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

abstract class DumpTasksWithOutputJarsTask : DefaultTask() {
    @TaskAction
    fun dumpTasksWithOutputJars() {
        project.tasks.forEach { task ->
            val files = task.outputs.files.filter { it.extension == "jar" }
            if (files.isEmpty.not()) {
                val output = buildString {
                    appendLine(task.name)
                    files.map { it.path }.forEach {
                        appendLine("- $it")
                    }
                }
                println(output)
            }
        }
    }
}
