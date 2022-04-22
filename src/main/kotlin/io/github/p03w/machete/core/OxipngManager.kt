package io.github.p03w.machete.core

import io.github.p03w.machete.util.invokeProcess
import io.github.p03w.machete.util.resolveAndMake
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

object OxipngManager {
    lateinit var tempDir: File
    val platform: Platform
    lateinit var oxipng: String

    init {
        val osName = System.getProperty("os.name")
        platform = when {
            osName.startsWith("Windows") -> Platform.WINDOWS
            osName.startsWith("Max") || osName.startsWith("Darwin") -> Platform.APPLE
            else -> Platform.LINUX
        }
    }

    fun unpackOxipng() {
        val oxipng = when (platform) {
            Platform.WINDOWS -> this::class.java.getResourceAsStream("/oxipng/oxipng-windows.exe")
            Platform.APPLE -> this::class.java.getResourceAsStream("/oxipng/oxipng-apple")
            Platform.LINUX -> this::class.java.getResourceAsStream("/oxipng/oxipng-linux")
        } ?: throw IllegalStateException("Could not unpack oxipng binary for platform ${platform.name}")

        val file = if (platform == Platform.WINDOWS) {
            tempDir.resolveAndMake("oxipng.exe")
        } else {
            tempDir.resolveAndMake("oxipng")
        }

        println("Detected platform is $platform")
        Files.copy(oxipng, file.toPath(), StandardCopyOption.REPLACE_EXISTING)
        this.oxipng = file.absolutePath
    }

    fun optimize(file: File) {
        invokeProcess(oxipng,
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
