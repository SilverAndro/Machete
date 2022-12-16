package io.github.p03w.machete.core.passes

import io.github.p03w.machete.config.MachetePluginExtension
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.ClassNode
import org.slf4j.Logger
import java.io.File
import java.util.*

object ClassFilePass : JarOptimizationPass {
    private enum class StripData {
        LVT,
        SOURCE_FILE
    }

    override fun shouldRunOnFile(file: File, config: MachetePluginExtension, log: Logger): Boolean {
        val ext = file.extension
        return ext == "class" && (
                config.sourceFileStriping.enabled.get() ||
                config.lvtStriping.enabled.get()
        )
    }

    override fun processFile(file: File, config: MachetePluginExtension, log: Logger, workDir: File, project: Project) {
        val toStrip = EnumSet.noneOf(StripData::class.java)
        if (config.lvtStriping.enabled.get())        toStrip.add(StripData.LVT)
        if (config.sourceFileStriping.enabled.get()) toStrip.add(StripData.SOURCE_FILE)

        if (toStrip.isNotEmpty()) {
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
}