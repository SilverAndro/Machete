package io.github.p03w.machete.config

import io.github.p03w.machete.config.optimizations.JIJConfig
import io.github.p03w.machete.config.optimizations.JsonConfig
import io.github.p03w.machete.config.optimizations.PngConfig
import io.github.p03w.machete.config.optimizations.XmlConfig
import io.github.p03w.machete.config.optimizations.lossy.LVTStripConfig
import io.github.p03w.machete.config.optimizations.lossy.SourceFileStripConfig
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested

@Suppress("LeakingThis")
abstract class MachetePluginExtension {
    /**
     * A set of strings denoting additional tasks to pull output jars from
     */
    @get:Input
    abstract val additionalTasks: SetProperty<String>

    /**
     * A set of strings denoting tasks to exclude output jars from
     */
    @get:Input
    abstract val ignoredTasks: SetProperty<String>

    /**
     * If the plugin should do anything at all, mainly for debugging or other purposes
     */
    @get:Input
    abstract val enabled: Property<Boolean>

    /**
     * If the original files should be kept by writing optimized ones with "-optimized" at the end of the file name
     */
    @get:Input
    abstract val keepOriginal: Property<Boolean>

    @get:Nested
    abstract val json: JsonConfig

    @get:Nested
    abstract val png: PngConfig

    @get:Nested
    abstract val jij: JIJConfig

    @get:Nested
    abstract val xml: XmlConfig

    @get:Nested
    abstract val lvtStriping: LVTStripConfig

    @get:Nested
    abstract val sourceFileStriping: SourceFileStripConfig

    /**
     * What optimizations are enabled/disabled
     */
    @Suppress("DEPRECATION")
    @Deprecated("Please use optimization specific flags")
    @get:Nested
    abstract val optimizations: Optimizations

    // Upgrade old config, TODO remove me for 2.0.0
    @Suppress("DEPRECATION")
    fun upgrade() {
        if (optimizations.json.isPresent) json.enabled.convention(optimizations.json.get())
        if (optimizations.png.isPresent) png.enabled.convention(optimizations.png.get())
        if (optimizations.jarInJar.isPresent) jij.enabled.convention(optimizations.jarInJar.get())
        if (optimizations.stripLVT.isPresent) lvtStriping.enabled.convention(optimizations.stripLVT.get())
    }

    init {
        enabled.convention(true)
        keepOriginal.convention(false)
    }
}
