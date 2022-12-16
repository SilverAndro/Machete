package io.github.p03w.machete.core

import io.github.p03w.machete.config.MachetePluginExtension
import io.github.p03w.machete.core.passes.*
import io.github.p03w.machete.util.allWithExtension
import io.github.p03w.machete.util.resolveAndMake
import io.github.p03w.machete.util.resolveAndMakeSiblingDir
import io.github.p03w.machete.util.unzip
import org.gradle.api.Project
import java.io.File
import java.nio.file.Files
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
    val project: Project,
    val isChild: Boolean = false
) {
    private val children = mutableMapOf<String, File>()
    private val toIgnore = mutableListOf<String>()

    private val log = project.logger

    private val passes = buildList {
        if (config.png.enabled.get()) add(PngPass)
        if (config.json.enabled.get()) add(JsonPass)
        if (config.xml.enabled.get()) add(XmlPass)
        add(ClassFilePass)
    }

    fun unpack() {
        JarFile(file).use { jarFile ->
            jarFile.manifest?.entries?.forEach { (t, u) ->
                // File is signed! JVM will throw some nasty errors if we change this file at all and try to launch
                if (u.entries.find { it.key.toString().contains("Digest") } != null) {
                    toIgnore.add(t.split("/").last())
                    log.info("[${project.name}] Will skip file ${t.split("/").last()} as it is signed")
                }
            }
        }

        ZipInputStream(file.inputStream().buffered()).use {
            it.unzip(workDir)
        }
    }

    private fun optimizeJarInJar() {
        workDir.allWithExtension("jar", config.jij.extraFileExtensions.get(), toIgnore) { file ->
            val unpack =
                JarOptimizer(workDir.resolveAndMakeSiblingDir(file.nameWithoutExtension), file, config, project, true)
            unpack.unpack()
            unpack.optimize()

            val outJar = workDir.resolveAndMakeSiblingDir("tmpJars").resolveAndMake(file.name)

            unpack.repackTo(outJar)
            children[file.relativeTo(workDir).path] = outJar
        }
    }

    fun optimize() {
        passes.forEach {
            workDir.walkBottomUp().filter { !toIgnore.contains(it.name) }.forEach { file ->
                if (it.shouldRunOnFile(file, config, log)) {
                    it.processFile(file, config, log, workDir, project)
                }
            }
        }

        if (config.jij.enabled.get()) optimizeJarInJar()
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
}
