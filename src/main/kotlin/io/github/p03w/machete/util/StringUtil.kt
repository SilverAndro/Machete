package io.github.p03w.machete.util

import java.util.*

fun String.capital() = replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

fun Regex.matches(char: Char) = matches(char.toString())
