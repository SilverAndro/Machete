package io.github.p03w.machete

import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

@Suppress("LeakingThis")
abstract class MachetePluginExtension {
    /**
     * A set of strings denoting additional jars to process relative to build/libs/
     */
    abstract val additionalJars: ListProperty<String>

    /**
     * A set of strings denoting jars to exclude relative to build/libs/
     */
    abstract val ignoredJars: ListProperty<String>

    /**
     * If the original files should be kept by writing optimized ones with "-optimized" at the end of the file name
     */
    abstract val keepOriginal: Property<Boolean>

    init {
        keepOriginal.convention(false)
    }
}
