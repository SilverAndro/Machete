package io.github.p03w.machete.core

import io.github.p03w.machete.config.MachetePluginExtension
import io.github.p03w.machete.core.json.JsonMinifier
import io.github.p03w.machete.util.allWithExtension
import io.github.p03w.machete.util.resolveAndMake
import io.github.p03w.machete.util.resolveAndMakeSiblingDir
import io.github.p03w.machete.util.unzip
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.ClassNode
import java.io.File
import java.nio.file.Files
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarInputStream
import java.util.jar.JarOutputStream
import java.util.zip.Deflater

/**
 * Manages optimizing a jar
 */
class JarOptimizer(
    val workDir: File,
    val file: File,
    val config: MachetePluginExtension,
    val isChild: Boolean = false
) {
    private val children = mutableMapOf<String, File>()
    private val toIgnore = mutableListOf<String>()

    fun unpack() {
        JarFile(file).use {
            it.manifest.entries.forEach { (t, u) ->
                if (u.entries.find { it.key.toString().contains("Digest") } != null) {
                    toIgnore.add(t.split("/").last())
                }
            }
        }

        JarInputStream(file.inputStream().buffered()).use {
            it.unzip(workDir)
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

    private fun optimizeJarInJar() {
        workDir.allWithExtension("jar", toIgnore) { file ->
            val unpack = JarOptimizer(workDir.resolveAndMakeSiblingDir(file.nameWithoutExtension), file, config, true)
            unpack.unpack()
            unpack.optimize()

            val outJar = workDir.resolveAndMakeSiblingDir("tmpJars").resolveAndMake(file.name)

            unpack.repackTo(outJar)
            children[file.relativeTo(workDir).path] = outJar
        }
    }

    private fun stripLVT() {
        workDir.allWithExtension("class", toIgnore) { file ->
            val reader = file.inputStream().buffered().use {
                ClassReader(it)
            }
            val node = ClassNode()
            reader.accept(node, 0)

            node.methods.forEach {
                it.localVariables?.clear()
            }

            val writer = ClassWriter(0)
            node.accept(writer)
            file.outputStream().use { stream ->
                stream.write(writer.toByteArray())
            }
        }
    }

    fun optimize() {
        val opti = config.optimizations
        if (opti.jarInJar.get())   optimizeJarInJar()
        if (opti.png.get())        optimizePNG()
        if (opti.json.get())       optimizeJSON()

        if (opti.stripLVT.get())   stripLVT()
    }

    fun repackTo(file: File) {
        file.delete()
        val jar = JarOutputStream(file.outputStream().buffered())

        if (isChild) {
            jar.setLevel(Deflater.NO_COMPRESSION)
        } else {
            jar.setLevel(Deflater.BEST_COMPRESSION)
        }

        jar.use {
            fun File.pathInJar(): String {
                return this.relativeTo(workDir).path.replace("\\", "/")
            }

            // .jars are handled by the children list, so that we can place them properly
            workDir.walkBottomUp().toList().filter {
                it.isFile && (it.extension != "jar" || !config.optimizations.jarInJar.get())
            }.forEach { optimizedFile ->
                val entry = JarEntry(optimizedFile.pathInJar())
                jar.putNextEntry(entry)
                Files.copy(optimizedFile.toPath(), it)
                jar.closeEntry()
            }

            children.forEach { (path, childJar) ->
                val entry = JarEntry(path.replace("\\", "/"))
                jar.putNextEntry(entry)
                Files.copy(childJar.toPath(), it)
                jar.closeEntry()
            }
        }
    }
}
