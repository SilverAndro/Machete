package io.github.p03w.machete.util

import java.io.File

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

fun File.allWithExtension(ext: String, action: (File) -> Unit) {
    walkBottomUp().toList().filter { it.extension == ext }.forEach(action)
}
