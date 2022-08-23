package io.github.p03w.machete.patches

import io.github.p03w.machete.config.MachetePluginExtension
import org.gradle.api.Project

interface Patch {
    fun shouldApply(project: Project, config: MachetePluginExtension): Boolean
    fun patch(project: Project, config: MachetePluginExtension)
}
