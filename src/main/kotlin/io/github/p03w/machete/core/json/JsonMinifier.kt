package io.github.p03w.machete.core.json

import io.github.p03w.machete.util.matches

// Compliant with EMCA-404 rev. 2
// https://www.ecma-international.org/wp-content/uploads/ECMA-404_2nd_edition_december_2017.pdf
// Some lenience added for code simplicity and UX
class JsonMinifier(private val original: String) {
    private val input = JsonParserStringWrapper(original)
    private val root: JsonValue?

    init {
        root = parse(input)
    }

    private fun parse(input: JsonParserStringWrapper): JsonValue {
        input.takeAllOf(wsRegex)
        return when (input.current()) {
            '{' -> parseObject(input)
            '[' -> parseArray(input)
            else -> throw JsonFormatError("Unrecognized starting character \"${input.current()}\"")
        }
    }

    private fun parseAny(input: JsonParserStringWrapper): JsonValue {
        input.takeAllOf(wsRegex)
        return when (val start = input.current()) {
            '{' -> parseObject(input)
            '[' -> parseArray(input)
            '"' -> parseString(input)
            else -> {
                // Guess the type
                if (start == 't' || start == 'f' || start == 'n') return parseFixed(input)
                if (numberRegex.matches(start)) return parseNumber(input)
                throw JsonFormatError("Unknown type start \"$start\"")
            }
        }
    }

    private fun parseNumber(input: JsonParserStringWrapper): JsonNumberValue {
        input.takeAllOf(wsRegex)
        return JsonNumberValue(input.takeAllOf(numberRegex))
    }

    private fun parseFixed(input: JsonParserStringWrapper): JsonFixedValue {
        input.takeAllOf(wsRegex)
        return when (val taken = input.take()) {
            't' -> {
                input.take('r'); input.take('u'); input.take('e')
                JsonFixedValue("true")
            }
            'f' -> {
                input.take('a'); input.take('l'); input.take('s'); input.take('e')
                JsonFixedValue("false")
            }
            'n' -> {
                input.take('u'); input.take('l'); input.take('l')
                JsonFixedValue("null")
            }
            else -> throw JsonFormatError("Unknown fixed starting character \"$taken\"")
        }
    }

    private fun parseObject(input: JsonParserStringWrapper): JsonObjectValue {
        input.takeAllOf(wsRegex)
        val obj = JsonObjectValue()

        input.take('{')

        while (true) {
            input.takeAllOf(wsRegex)
            when (input.current()) {
                '"' -> {
                    val key = parseString(input)
                    input.takeAllOf(wsRegex)
                    input.take(':')
                    val value = parseAny(input)
                    obj.put(key, value)
                }
                ',' -> input.take()
                '}' -> break
                else -> throw JsonFormatError("Unknown character in object definition \"${input.peek()}\"")
            }
        }

        input.take('}')

        return obj
    }

    private fun parseArray(input: JsonParserStringWrapper): JsonArrayValue {
        input.takeAllOf(wsRegex)

        val array = JsonArrayValue()

        input.take('[')

        while (true) {
            input.takeAllOf(wsRegex)
            when (input.current()) {
                ',' -> input.take()
                ']' -> break
                else -> {
                    val value = parseAny(input)
                    array.put(value)
                }
            }
        }

        input.take(']')

        return array
    }

    private fun parseString(input: JsonParserStringWrapper): JsonStringValue {
        input.takeAllOf(wsRegex)
        return JsonStringValue(buildString {
            append(input.take('"'))
            // Handle escaping
            // It just so happens that (any odd number of escapes) = (1 true quote escape)
            // So count up the escapes and eat until the next quote if odd
            do {
                append(input.takeUntil('"'))
                val escapeCount = input.countBeforeOf('\\')
            } while (escapeCount % 2 == 1)
        })
    }

    override fun toString(): String {
        return root?.getOutput() ?: original
    }

    companion object {
        val wsRegex = Regex("[ \r\n\t]")
        val numberRegex = Regex("[-\\d.eE+]")
    }
}
