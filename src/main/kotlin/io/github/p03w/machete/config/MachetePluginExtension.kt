package io.github.p03w.machete.config

import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Nested

@Suppress("LeakingThis")
abstract class MachetePluginExtension {
    /**
     * A set of strings denoting additional tasks to pull output jars from
     */
    abstract val additionalTasks: SetProperty<String>

    /**
     * A set of strings denoting tasks to exclude output jars from
     */
    abstract val ignoredTasks: SetProperty<String>

    /**
     * If the original files should be kept by writing optimized ones with "-optimized" at the end of the file name
     */
    abstract val keepOriginal: Property<Boolean>

    /**
     * What optimizations are enabled/disabled
     */
    @get:Nested
    abstract val optimizations: Optimizations

    init {
        keepOriginal.convention(false)
    }
}
