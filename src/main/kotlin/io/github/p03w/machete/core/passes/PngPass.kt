package io.github.p03w.machete.core.passes

import io.github.p03w.machete.config.MachetePluginExtension
import io.github.p03w.machete.core.OxipngManager
import org.gradle.api.Project
import org.slf4j.Logger
import java.io.File

object PngPass : JarOptimizationPass {
    override fun shouldRunOnFile(file: File, config: MachetePluginExtension, log: Logger): Boolean {
        val ext = file.extension
        return ext == "png" || config.png.extraFileExtensions.get().contains(ext)
    }

    override fun processFile(file: File, config: MachetePluginExtension, log: Logger, workDir: File, project: Project) {
        try {
            OxipngManager.optimize(file, config.png, project.name)
        } catch (err: Throwable) {
            log.warn("Failed to optimize ${file.relativeTo(workDir).path}")
            err.printStackTrace()
        }
    }
}