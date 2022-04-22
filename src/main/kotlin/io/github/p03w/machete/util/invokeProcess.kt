package io.github.p03w.machete.util

fun invokeProcess(vararg args: String) {
    val process = ProcessBuilder(*args).also {
        it.redirectOutput(ProcessBuilder.Redirect.INHERIT)
        it.redirectError(ProcessBuilder.Redirect.INHERIT)
    }.start()
    process.waitFor()
}
