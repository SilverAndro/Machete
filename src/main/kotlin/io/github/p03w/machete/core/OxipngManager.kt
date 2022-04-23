package io.github.p03w.machete.core

import io.github.p03w.machete.util.invokeProcess
import io.github.p03w.machete.util.resolveAndMake
import io.github.p03w.machete.util.resolveAndMakeSibling
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

object OxipngManager {
    lateinit var tempDir: File
    private val platform: Platform
    private lateinit var oxipng: String

    init {
        val osName = System.getProperty("os.name")
        platform = when {
            osName.startsWith("Windows") -> Platform.WINDOWS
            osName.startsWith("Mac") || osName.startsWith("Darwin") -> Platform.APPLE
            else -> Platform.LINUX
        }
    }

    fun unpackOxipng() {
        val oxipng = when (platform) {
            Platform.WINDOWS -> this::class.java.getResourceAsStream("/oxipng/oxipng-windows.exe")
            Platform.APPLE -> this::class.java.getResourceAsStream("/oxipng/oxipng-apple")
            Platform.LINUX -> this::class.java.getResourceAsStream("/oxipng/oxipng-linux")
        } ?: throw IllegalStateException("Could not unpack oxipng binary for platform ${platform.name}")

        val license = this::class.java.getResourceAsStream("/oxipng/OXIPNG_LICENSE")

        val file = if (platform == Platform.WINDOWS) {
            tempDir.resolveAndMake("oxipng.exe")
        } else {
            tempDir.resolveAndMake("oxipng")
        }

        println("Detected platform is $platform")
        Files.copy(oxipng, file.toPath(), StandardCopyOption.REPLACE_EXISTING)
        if (license != null) {
            Files.copy(
                license,
                file.resolveAndMakeSibling("OXIPNG_LICENSE").toPath(),
                StandardCopyOption.REPLACE_EXISTING
            )
        } else {
            println("Failed to unpack oxipng license, oxipng is licensed under the MIT license, Copyright (c) 2016 Joshua Holmer")
        }
        this.oxipng = file.absolutePath
    }

    fun optimize(file: File) {
        invokeProcess(
            oxipng,
            file.absolutePath,
            "-o", "max",          // Maximum optimization level
            "--strip", "all",     // Strip all metadata
            "-a",                 // Alpha optimizations,
            "--out", file.absolutePath
        )
    }

    enum class Platform {
        WINDOWS,
        APPLE,
        LINUX
    }
}
