package io.github.p03w.machete.core

import io.github.p03w.machete.core.json.JsonMinifier
import io.github.p03w.machete.util.allWithExtension
import io.github.p03w.machete.util.resolveAndMake
import io.github.p03w.machete.util.resolveAndMakeSiblingDir
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.model.enums.CompressionLevel
import net.lingala.zip4j.model.enums.CompressionMethod
import java.io.File
import java.util.jar.JarFile

/**
 * Manages optimizing a jar
 */
class JarOptimizer(val workDir: File, val file: File, val isChild: Boolean = false) {
    private val children = mutableMapOf<String, File>()
    private val toIgnore = mutableListOf<String>()

    fun unpack() {
        JarFile(file).use {
            it.manifest.entries.forEach { (t, u) ->
                if (u.entries.find { it.key.toString().contains("Digest") } != null) {
                    toIgnore.add(t.split("/").last())
                    println("Will not optimize ${t.split("/").last()} because it has a digital signature attached")
                }
            }
        }
        ZipFile(file).use {
            it.extractAll(workDir.path)
        }
    }

    private fun optimizePNG() {
        workDir.allWithExtension("png", toIgnore) {
            try {
                OxipngManager.optimize(it)
            } catch (err: Throwable) {
                println("Failed to optimize ${file.relativeTo(workDir).path}")
                err.printStackTrace()
            }
        }
    }

    private fun optimizeJSON() {
        workDir.allWithExtension("json", toIgnore) { file ->
            val text = file.bufferedReader().use {
                it.readText()
            }
            file.bufferedWriter().use {
                try {
                    val final = JsonMinifier(text)
                    it.write(final.toString())
                } catch (err: Throwable) {
                    println("Failed to optimize ${file.relativeTo(workDir).path}")
                    err.printStackTrace()
                    it.write(text)
                }
            }
        }
    }

    private fun optimizeChildren() {
        workDir.allWithExtension("jar", toIgnore) { file ->
            val unpack = JarOptimizer(workDir.resolveAndMakeSiblingDir(file.nameWithoutExtension), file, true)
            unpack.unpack()
            unpack.optimize()

            val outJar = workDir.resolveAndMakeSiblingDir("tmpJars").resolveAndMake(file.name)

            unpack.repackTo(outJar)
            children[file.relativeTo(workDir).path] = outJar
        }
    }

    fun optimize() {
        optimizeChildren()
        optimizePNG()
        optimizeJSON()
    }

    fun repackTo(file: File) {
        file.delete()
        val zip = ZipFile(file)

        zip.use {
            fun makeParams(nameInZip: String): ZipParameters {
                val params = ZipParameters()
                params.lastModifiedFileTime = 0
                // STORE allows any common classes/references in libraries to be
                // Compressed alongside instead of sitting compressed itself
                if (isChild) {
                    params.compressionMethod = CompressionMethod.STORE
                    params.compressionLevel = CompressionLevel.NO_COMPRESSION
                } else {
                    // Cronch
                    params.compressionMethod = CompressionMethod.DEFLATE
                    params.compressionLevel = CompressionLevel.ULTRA
                }

                // Fix windows, because OF COURSE its windows
                // I totally didn't spend hours trying to debug why classloading was failing
                // Opening the output in dozens of different .zip explorers
                // Even locating AZip, a 20-year-old ADA based program for zip exploration
                // Only for the issue to be that
                // EVERY
                // SINGLE
                // ONE
                // Was smarter than the java implementation, and could "fix" file paths
                // Screw windows, and screw legacy code
                //
                // Fun fact! The java implementation is SO OLD
                // That it literally ONLY supports STORE and DEFLATE
                // Other compression methods were first  added in the LATE 1990s!!!!
                // YOU HAVE HAD OVER 20 YEARS AT THE TIME OF WRITING TO SUPPORT ANYTHING BETTER
                //
                // *sigh*
                params.fileNameInZip = nameInZip.replace("\\", "/")
                return params
            }

            // .jars are handled by the children list, so that we can place them properly
            workDir.walkBottomUp().toList().filter { it.isFile && it.extension != "jar" }.forEach { optimizedFile ->
                zip.addFile(optimizedFile, makeParams(optimizedFile.relativeTo(workDir).path))
            }

            children.forEach { (path, childJar) ->
                zip.addFile(childJar, makeParams(path))
            }
        }
    }
}
