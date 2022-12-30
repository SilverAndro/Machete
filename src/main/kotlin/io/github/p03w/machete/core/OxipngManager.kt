package io.github.p03w.machete.core

import io.github.p03w.machete.config.optimizations.PngConfig
import io.github.p03w.machete.util.*
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.PosixFilePermissions
import kotlin.io.path.absolute

/**
 * Manages the Oxipng file
 */
object OxipngManager {
    lateinit var tempDir: File
    private val platform: Platform
    private val useNative = doesProcessRun("oxipng")
    private lateinit var oxipng: String

    private val logger = LoggerFactory.getLogger("Machete")

    init {
        // Get the OS name
        val osName = System.getProperty("os.name")
        // Try to guess the platform
        platform = when {
            osName.startsWith("Windows") -> Platform.WINDOWS
            osName.startsWith("Mac") || osName.startsWith("Darwin") -> Platform.APPLE
            else -> Platform.LINUX
        }
    }

    fun unpackOxipng(name: String) {
        if (useNative) {
            logger.info("Using native oxipng install instead of unpacking")
            return
        }

        // Get the file specific to this platform
        val oxipngStream = when (platform) {
            Platform.WINDOWS -> this::class.java.getResourceAsStream("/oxipng/oxipng-windows.exe")
            Platform.APPLE -> this::class.java.getResourceAsStream("/oxipng/oxipng-apple")
            Platform.LINUX -> this::class.java.getResourceAsStream("/oxipng/oxipng-linux")
        } ?: throw IllegalStateException("Could not unpack oxipng binary for platform ${platform.name}")

        // Grab the license
        val licenseStream = this::class.java.getResourceAsStream("/oxipng/OXIPNG_LICENSE")

        // Make sure its .exe if windows, executable if linux/mac
        val file = if (platform == Platform.WINDOWS) {
            tempDir.resolveAndMake("oxipng.exe")
        } else {
            tempDir.resolveAndMake("oxipng")
        }

        logger.info("Detected platform is $platform for project $name")
        // Copy out oxipng, overwriting any previous copies
        Files.copy(oxipngStream, file.toPath().absolute(), StandardCopyOption.REPLACE_EXISTING)
        // Try to make the file executable
        file.setExecutable(true)
        if (platform != Platform.WINDOWS) {
            // Linux D:
            tryCatchAll(
                { invokeProcess("chmod", "+x", file.absolutePath) },
                { Files.setPosixFilePermissions(file.toPath(), PosixFilePermissions.fromString("rwxr-x---")) }
            ) { it.printStackTrace() }
        }
        if (file.canExecute().not()) {
            logger.error("Could not set executable bit for oxipng!\n" +
                    "Seems you've run into an edge case I didn't consider :(\n" +
                    "I've disabled png optimization due to this error, open an issue?")
            return
        }

        // Unpack or display the license, in compliance with the MIT license which states:
        // "The above copyright notice and this permission notice shall be included in all
        // copies or substantial portions of the Software."
        if (licenseStream != null) {
            Files.copy(
                licenseStream,
                file.resolveAndMakeSibling("OXIPNG_LICENSE").toPath(),
                StandardCopyOption.REPLACE_EXISTING
            )
        } else {
            logger.warn("Failed to unpack oxipng license, oxipng is licensed under the MIT license, Copyright (c) 2016 Joshua Holmer")
        }

        logger.info("Oxipng should be at ${file.absolutePath}")
        if (file.exists().not()) {
            logger.error("Oxipng didn't get created? Should be at ${file.absolutePath} but OS reports file doesn't exist, disabled png optimization.")
            return
        }

        this.oxipng = file.absolutePath
    }

    fun optimize(file: File, config: PngConfig, name: String) {
        if (this::oxipng.isInitialized || useNative) {
            if (!useNative && File(oxipng).exists().not()) {
                if (config.expectReunpack.get()) {
                    logger.warn("Oxipng binary was removed? Re-unpacking in attempt to recover (use `png.expectReunpack = true` to suppress)")
                }
                unpackOxipng(name)
            }

            val args = mutableListOf(
                if (!useNative) oxipng else "oxipng",
                file.absolutePath,
                "-o", config.optimizationLevel.get().toString(),
                "--out", file.absolutePath
            )

            if (config.strip.get() != PngConfig.Strip.NONE) {
                args.add("--strip")
                args.add(config.strip.get().flag)
            }

            if (config.alpha.get()) {
                args.add("-a")
            }

            // Run oxipng with every optimization enabled
            invokeProcess(*args.toTypedArray())
        }
    }

    enum class Platform {
        WINDOWS,
        APPLE,
        LINUX
    }
}
