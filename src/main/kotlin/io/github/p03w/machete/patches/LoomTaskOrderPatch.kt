package io.github.p03w.machete.patches

import org.gradle.api.Project

/**
 * A patch that fixes some task ordering issues with loom that disabled
 * certain gradle build optimizations
 */
object LoomTaskOrderPatch: Patch {
    override fun shouldApply(project: Project): Boolean {
        return project.plugins.hasPlugin("fabric-loom")
    }

    override fun patch(project: Project) {
        val remap = project.tasks.findByName("remapJar")
        val optimizeJar = project.tasks.findByName("optimizeOutputsOfJar")
        if (remap == null) {
            project.logger.error("LoomTaskOrderPatch expected the remapJar task to exist but it did not! Cancelling patch application")
            return
        }
        // This could not exist for many intentional reasons, so just silently drop
        if (optimizeJar == null) return

        // Explicit dependency
        remap.dependsOn(optimizeJar)
    }
}
