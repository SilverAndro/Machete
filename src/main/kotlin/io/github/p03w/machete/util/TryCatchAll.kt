package io.github.p03w.machete.util

inline fun tryCatchAll(vararg actions: () -> Unit, handler: (Throwable) -> Unit) {
    actions.forEach {
        try {
            it()
        } catch (err: Throwable) {
            handler(err)
        }
    }
}
