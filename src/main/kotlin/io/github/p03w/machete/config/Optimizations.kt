package io.github.p03w.machete.config

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional

@Deprecated(
    message = "Enable flags will be removed and moved into optimization-specific flags (i.e optimizationName.enabled)",
    level = DeprecationLevel.WARNING
)
abstract class Optimizations {
    /**
     * If nested jar file optimizations should be applied
     */
    @Deprecated(
        message = "Enable flags will be removed and moved into optimization-specific flags (i.e optimizationName.enabled)",
        level = DeprecationLevel.WARNING
    )
    @get:Optional
    @get:Input
    abstract val jarInJar: Property<Boolean>

    /**
     * If png file optimizations should be applied
     */
    @Deprecated(
        message = "Enable flags will be removed and moved into optimization-specific flags (i.e optimizationName.enabled)",
        level = DeprecationLevel.WARNING
    )
    @get:Optional
    @get:Input
    abstract val png: Property<Boolean>

    /**
     * If json file optimizations should be applied
     */
    @Deprecated(
        message = "Enable flags will be removed and moved into optimization-specific flags (i.e optimizationName.enabled)",
        level = DeprecationLevel.WARNING
    )
    @get:Optional
    @get:Input
    abstract val json: Property<Boolean>

    /**
     * If the local variable table should be stripped
     *
     * Lossy on JVM 14+ as this table is used in the "helpful NPE messages" functionality
     *
     * [https://openjdk.java.net/jeps/358](https://openjdk.java.net/jeps/358)
     */
    @Deprecated(
        message = "Enable flags will be removed and moved into optimization-specific flags (i.e optimizationName.enabled)",
        level = DeprecationLevel.WARNING
    )
    @get:Optional
    @get:Input
    abstract val stripLVT: Property<Boolean>
}
