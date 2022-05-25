package io.github.p03w.machete.patches

import org.gradle.api.Project

interface Patch {
    fun shouldApply(project: Project): Boolean
    fun patch(project: Project)
}
