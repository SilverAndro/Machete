package io.github.p03w.machete.util

import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.zip.ZipInputStream

fun File.resolveAndMakeSiblingDir(relativeFile: String): File {
    val res = resolveSibling(relativeFile)
    res.mkdirs()
    return res
}

fun File.resolveAndMakeSibling(relativeFile: String): File {
    val res = resolveSibling(relativeFile)
    res.createNewFile()
    return res
}

fun File.resolveAndMake(relativeFile: String): File {
    val res = resolve(relativeFile)
    res.createNewFile()
    return res
}

fun File.allWithExtension(ext: String, action: (File)->Unit) {
    walkBottomUp().toList().filter { it.extension == ext }.forEach(action)
}

fun ZipInputStream.unzip(outputDir: File) {
    var entry = nextEntry
    while (entry != null) {
        val resolvedPath = outputDir.resolve(entry.name).normalize().toPath()
        if (!resolvedPath.startsWith(outputDir.path)) { throw RuntimeException("Zip slip somehow, don't do that: " + entry.name) }

        if (entry.isDirectory) {
            Files.createDirectories(resolvedPath)
        } else {
            Files.createDirectories(resolvedPath.parent)
            Files.copy(this, resolvedPath, StandardCopyOption.REPLACE_EXISTING)
        }

        entry = nextEntry
    }
}
