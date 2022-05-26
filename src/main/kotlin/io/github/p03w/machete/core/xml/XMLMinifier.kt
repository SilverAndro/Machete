package io.github.p03w.machete.core.xml

class XMLMinifier(private val original: String) {
    override fun toString(): String {
        return emptyTagRegex.replace(original, "$1")
    }

    companion object {
        val emptyTagRegex = Regex("(<.*?>)\\s*")
    }
}
