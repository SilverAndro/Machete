package io.github.p03w.machete.util

import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.zip.ZipInputStream

/**
 * Unzips a zip file to a directory
 */
fun ZipInputStream.unzip(outputDir: File) {
    var entry = nextEntry
    while (entry != null) {
        val resolvedPath = outputDir.resolve(entry.name).normalize().toPath()
        if (!resolvedPath.startsWith(outputDir.path)) {
            throw RuntimeException("Zip slip somehow, don't do that: " + entry.name)
        }

        if (entry.isDirectory) {
            Files.createDirectories(resolvedPath)
        } else {
            Files.createDirectories(resolvedPath.parent)
            Files.copy(this, resolvedPath, StandardCopyOption.REPLACE_EXISTING)
        }

        entry = nextEntry
    }
}

/**
 * Resolves a sibling directory, calls .mkdirs, and returns the new directory
 */
fun File.resolveAndMakeSiblingDir(relativeFile: String): File {
    val res = resolveSibling(relativeFile)
    res.mkdirs()
    return res
}

/**
 * Resolves a directory, calls .mkdirs, and returns the new directory
 */
fun File.resolveAndMakeDir(relativeFile: String): File {
    val res = resolve(relativeFile)
    res.mkdirs()
    return res
}

/**
 * Resolves a sibling file, calls .createNewFile, and returns the new file
 */
fun File.resolveAndMakeSibling(relativeFile: String): File {
    val res = resolveSibling(relativeFile)
    res.createNewFile()
    return res
}

/**
 * Resolves a file, calls .createNewFile, and returns the new file
 */
fun File.resolveAndMake(relativeFile: String): File {
    val res = resolve(relativeFile)
    res.createNewFile()
    return res
}

/**
 * Walks the entire sub file tree bottom-up, and runs [action] on any file with the specified extension
 */
inline fun File.allWithExtension(
    ext: String,
    extra: List<String> = listOf(),
    ignoreList: List<String>,
    action: (File) -> Unit
) {
    walkBottomUp()
        .toList()
        .filter {
            (it.extension == ext || extra.any {extra -> it.name.contains(extra)}) && !ignoreList.contains(it.name)
        }
        .forEach(action)
}
