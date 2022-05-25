package io.github.p03w.machete.core.json

import io.github.p03w.machete.util.matches

class JsonParserStringWrapper(private val string: String) {
    private var index = 0

    fun current() = string[index]
    fun peek() = string[index + 1]
    fun take() = string[index++]
    fun take(required: Char) = take().also {
        if (it != required) throw JsonFormatError("Expected to read \"$required\" but read \"$it\"")
    }

    fun takeUntil(delim: Char): String {
        return buildString {
            while (current() != delim) {
                append(take())
            }
            append(take())
        }
    }

    fun countBeforeOf(char: Char): Int {
        var count = 0
        for (x in index-2 downTo 0) {
            if (string[x] == char) count++ else return count
        }
        return count
    }

    fun takeAllOf(regex: Regex): String {
        return buildString {
            while (regex.matches(current())) {
                append(take())
            }
        }
    }

    override fun toString(): String {
        return string.substring(index)
    }
}
