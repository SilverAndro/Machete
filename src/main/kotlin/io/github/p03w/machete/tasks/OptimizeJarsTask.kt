package io.github.p03w.machete.tasks

import io.github.p03w.machete.config.MachetePluginExtension
import io.github.p03w.machete.core.JarOptimizer
import io.github.p03w.machete.util.resolveAndMakeSibling
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class OptimizeJarsTask : DefaultTask() {
    @get:Input
    abstract val buildDir: Property<File>

    @get:Nested
    abstract val extension: Property<MachetePluginExtension>

    @TaskAction
    fun optimizeJars() {
        inputs.files.forEach {
            val tempJarDir = buildDir.get().resolve("root-jar")
            tempJarDir.deleteRecursively()
            tempJarDir.mkdirs()

            val optimizer = JarOptimizer(tempJarDir, it, extension.get(), project)
            optimizer.unpack()
            optimizer.optimize()

            if (extension.get().keepOriginal.get()) {
                optimizer.repackTo(it.resolveAndMakeSibling(it.nameWithoutExtension + "-optimized.jar"))
            } else {
                optimizer.repackTo(it)
            }
        }
    }
}
