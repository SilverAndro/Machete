package io.github.p03w.machete.core

import io.github.p03w.machete.config.MachetePluginExtension
import io.github.p03w.machete.core.json.JsonMinifier
import io.github.p03w.machete.core.xml.XMLMinifier
import io.github.p03w.machete.util.allWithExtension
import io.github.p03w.machete.util.resolveAndMake
import io.github.p03w.machete.util.resolveAndMakeSiblingDir
import io.github.p03w.machete.util.unzip
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.ClassNode
import java.io.File
import java.nio.file.Files
import java.util.EnumSet
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.Deflater
import java.util.zip.ZipInputStream

/**
 * Manages optimizing a jar
 */
class JarOptimizer(
    val workDir: File,
    val file: File,
    val config: MachetePluginExtension,
    val project: Project? = null,
    val isChild: Boolean = false
) {
    private val children = mutableMapOf<String, File>()
    private val toIgnore = mutableListOf<String>()

    private val log = project?.logger

    fun unpack() {
        JarFile(file).use { jarFile ->
            jarFile.manifest?.entries?.forEach { (t, u) ->
                // File is signed! JVM will throw some nasty errors if we change this file at all and try to launch
                if (u.entries.find { it.key.toString().contains("Digest") } != null) {
                    toIgnore.add(t.split("/").last())
                    log?.info("[${project!!.name}] Will skip file ${t.split("/").last()} as it is signed")
                }
            }
        }

        ZipInputStream(file.inputStream().buffered()).use {
            it.unzip(workDir)
        }
    }

    private fun optimizePNG() {
        workDir.allWithExtension("png", toIgnore) {
            try {
                OxipngManager.optimize(it, config.png)
            } catch (err: Throwable) {
                log?.warn("Failed to optimize ${file.relativeTo(workDir).path}")
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
                    log?.warn("Failed to optimize ${file.relativeTo(workDir).path}")
                    err.printStackTrace()
                    it.write(text)
                }
            }
        }
    }

    private fun optimizeJarInJar() {
        workDir.allWithExtension("jar", toIgnore) { file ->
            val unpack =
                JarOptimizer(workDir.resolveAndMakeSiblingDir(file.nameWithoutExtension), file, config, project, true)
            unpack.unpack()
            unpack.optimize()

            val outJar = workDir.resolveAndMakeSiblingDir("tmpJars").resolveAndMake(file.name)

            unpack.repackTo(outJar)
            children[file.relativeTo(workDir).path] = outJar
        }
    }

    private fun optimizeXML() {
        workDir.allWithExtension("xml", toIgnore) { file ->
            val text = file.bufferedReader().use {
                it.readText()
            }
            file.bufferedWriter().use {
                try {
                    val final = XMLMinifier(text)
                    it.write(final.toString())
                } catch (err: Throwable) {
                    log?.warn("Failed to optimize ${file.relativeTo(workDir).path}")
                    err.printStackTrace()
                    it.write(text)
                }
            }
        }
    }

    private fun stripData(toStrip: EnumSet<StripData>) {
        workDir.allWithExtension("class", toIgnore) { file ->
            val reader = file.inputStream().buffered().use {
                ClassReader(it)
            }
            val node = ClassNode()
            reader.accept(node, 0)

            if (toStrip.contains(StripData.SOURCE_FILE)) node.sourceFile = null

            if (toStrip.contains(StripData.LVT)) {
                node.methods.forEach {
                    it.localVariables?.clear()
                }
            }

            val writer = ClassWriter(0)
            node.accept(writer)
            file.outputStream().use { stream ->
                stream.write(writer.toByteArray())
            }
        }
    }

    fun optimize() {
        if (config.jij.enabled.get()) optimizeJarInJar()
        if (config.png.enabled.get()) optimizePNG()
        if (config.json.enabled.get()) optimizeJSON()
        if (config.xml.enabled.get()) optimizeXML()

        val toStrip = EnumSet.noneOf(StripData::class.java)
        if (config.lvtStriping.enabled.get())        toStrip.add(StripData.LVT)
        if (config.sourceFileStriping.enabled.get()) toStrip.add(StripData.SOURCE_FILE)
        if (toStrip.isNotEmpty()) {
            stripData(toStrip)
        }
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
                it.isFile && (it.extension != "jar" || !config.jij.enabled.get())
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

    private enum class StripData {
        LVT,
        SOURCE_FILE
    }
}
