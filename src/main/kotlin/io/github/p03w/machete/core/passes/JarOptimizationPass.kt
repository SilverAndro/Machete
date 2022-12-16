package io.github.p03w.machete.core.passes

import io.github.p03w.machete.config.MachetePluginExtension
import org.gradle.api.Project
import org.slf4j.Logger
import java.io.File

interface JarOptimizationPass {
    fun shouldRunOnFile(file: File, config: MachetePluginExtension, log: Logger): Boolean
    fun processFile(file: File, config: MachetePluginExtension, log: Logger, workDir: File, project: Project)
}