package io.github.p03w.machete.config.optimizations

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input

@Suppress("LeakingThis")
abstract class PngConfig {
    @get:Input
    abstract val enabled: Property<Boolean>

    @get:Input
    abstract val optimizationLevel: Property<Int>

    @get:Input
    abstract val strip: Property<Strip>

    @get:Input
    abstract val alpha: Property<Boolean>

    init {
        enabled.convention(true)
        optimizationLevel.convention(4)
        strip.convention(Strip.ALL)
        alpha.convention(true)
    }

    enum class Strip(val flag: String) {
        NONE(""),
        SAFE("safe"),
        ALL("all")
    }
}
