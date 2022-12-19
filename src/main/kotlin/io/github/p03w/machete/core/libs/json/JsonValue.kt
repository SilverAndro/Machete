package io.github.p03w.machete.core.libs.json

sealed class JsonValue {
    abstract fun getOutput(): String
}

class JsonFixedValue(val representation: String) : JsonValue() {
    override fun getOutput(): String {
        return representation
    }
}

class JsonNumberValue(val representation: String) : JsonValue() {
    override fun getOutput(): String {
        return representation
    }
}

class JsonStringValue(val representation: String) : JsonValue() {
    override fun getOutput(): String {
        return representation
    }
}

class JsonArrayValue : JsonValue() {
    private val list = mutableListOf<JsonValue>()

    fun put(value: JsonValue) {
        list.add(value)
    }

    override fun getOutput(): String {
        return buildString {
            append("[")

            list.forEachIndexed { index, value ->
                append(value.getOutput())

                if (index != list.lastIndex) {
                    append(",")
                }
            }

            append("]")
        }
    }
}

class JsonObjectValue : JsonValue() {
    // Can have duplicates, and there isn't really a reasonable de-duplication strategy that won't upset someone
    private val map = mutableListOf<Pair<JsonStringValue, JsonValue>>()

    fun put(key: JsonStringValue, value: JsonValue) {
        map.add(key to value)
    }

    override fun getOutput(): String {
        return buildString {
            append("{")

            map.forEachIndexed { index, pair ->
                append(pair.first.getOutput())
                append(":")
                append(pair.second.getOutput())

                if (index != map.lastIndex) {
                    append(",")
                }
            }

            append("}")
        }
    }
}
