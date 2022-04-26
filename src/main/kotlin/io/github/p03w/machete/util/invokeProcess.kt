package io.github.p03w.machete.util

/**
 * A utility method that starts a process with output and error redirected to the current
 * stdout and stderr before waiting for its termination
 */
fun invokeProcess(vararg args: String) {
    val process = ProcessBuilder(*args).also {
        it.redirectOutput(ProcessBuilder.Redirect.INHERIT)
        it.redirectError(ProcessBuilder.Redirect.INHERIT)
    }.start()
    process.waitFor()
}
