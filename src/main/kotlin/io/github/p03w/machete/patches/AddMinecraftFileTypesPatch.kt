package io.github.p03w.machete.patches

import io.github.p03w.machete.config.MachetePluginExtension
import org.gradle.api.Project

object AddMinecraftFileTypesPatch : Patch {
    private val minecraftPlugins = setOf(
        "net.minecraftforge.gradle",
        "fabric-loom",
        "org.quiltmc.loom"
    )

    override fun shouldApply(project: Project, config: MachetePluginExtension): Boolean {
        return minecraftPlugins.any {
            project.plugins.hasPlugin(it)
        }
    }

    override fun patch(project: Project, config: MachetePluginExtension) {
        config.json.extraFileExtensions.addAll(listOf(
            "mcmeta"
        ))
    }
}