package io.github.p03w.machete.config.optimizations

import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input

@Suppress("LeakingThis")
abstract class PngConfig {
    /**
     * A list of file extensions to also process as JSON files
     */
    @get:Input
    abstract val extraFileExtensions: ListProperty<String>

    @get:Input
    abstract val enabled: Property<Boolean>

    @get:Input
    abstract val optimizationLevel: Property<Int>

    @get:Input
    abstract val strip: Property<Strip>

    @get:Input
    abstract val alpha: Property<Boolean>

    @get:Input
    abstract val expectReunpack: Property<Boolean>

    init {
        enabled.convention(true)
        optimizationLevel.convention(4)
        strip.convention(Strip.ALL)
        alpha.convention(true)
        expectReunpack.convention(false)
    }

    @Suppress("unused")
    enum class Strip(val flag: String) {
        NONE(""),
        SAFE("safe"),
        ALL("all")
    }
}
