package io.github.p03w.machete.util

// Tasks that are known to produce .jar files that may be of actual interest to end users
val knownGoodTasks = setOf(
    // java-base
    "jar",

    // fabric-loom
    "remapJar",

    // shadow
    "shadowJar"
)
