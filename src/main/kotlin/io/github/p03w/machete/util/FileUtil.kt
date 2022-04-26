package io.github.p03w.machete.util

import java.io.File

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
inline fun File.allWithExtension(ext: String, action: (File) -> Unit) {
    walkBottomUp().toList().filter { it.extension == ext }.forEach(action)
}
