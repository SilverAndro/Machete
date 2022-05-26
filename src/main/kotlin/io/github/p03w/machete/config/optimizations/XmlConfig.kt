package io.github.p03w.machete.config.optimizations

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input

@Suppress("LeakingThis")
abstract class XmlConfig {
    @get:Input
    abstract val enabled: Property<Boolean>

    init {
        enabled.convention(true)
    }
}
