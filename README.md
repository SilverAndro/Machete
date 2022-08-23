# Machete

Machete is a gradle plugin that attempts to optimize the size of output JARs
through both individual file optimizations and overall compression increases inspired by the [Detonater](https://github.com/EnnuiL/Detonater) project.

Simply applying the plugin should be enough to apply any optimizations in a standard environment,
as it will collect output JARs that are known to be usable artifacts and optimize them after the `build` task.

**Please note that this plugin works best on high-resource density projects**. Code-heavy ones will have minimal success and may even inflate overall.

### Optimizations

- JSON files are minimized by reading+writing through a custom formatter, which strips any whitespace.
- PNG files are run through the [Oxipng](https://github.com/shssoichiro/oxipng) project on maximum compression and metadata removal
- Nested JAR files are unpacked and have the same optimizations run on them
- XML files have extra whitespace removed
- The final result is then compressed with DEFLATE level 9, providing modest overall compression (bytecode doesn't compress well unfortunately)

There are also some disabled-by-default optimizations as they are technically lossy on the behavior of the jar.

- Local Variable Table stripping, disabled because this table is used for the "helpful NPEs" feature in java 14+
- Source file stripping, disabled because this is used to give the file a class was compiled from in error messages

More optimizations are planned as well, feel free to open an issue!

### Installation

Machete is available on the gradle plugin portal under `io.github.p03w.machete`, simply apply
the plugin to your project, and it should work out of the box.

See [here](https://plugins.gradle.org/plugin/io.github.p03w.machete) for in-depth install instructions.

### Configuration

To configure the plugin, use the `machete` block. This allows you to

- Add or remove tasks to pull output JARs from (`additionalTasks`/`ignoredTasks`, also please consider opening a PR if you use these)
- Disable overwriting the original artifacts (`keepOriginal`)
- Enable or disable specific optimizations (`optimizations`)

An example full config may look like:
```
machete {
    // Also optimize the output of task "foo"
    additionalTasks.add("foo")
    // Do not optimize the output of "bar"
    ignoredTasks.add("bar")
    
    // Keep the original copies in the build directory
    keepOriginal = true
    
    // Disable the JIJ, PNG, and JSON optimizations
    jij.enabled = false
    png.enabled = false
    json.enabled = false
    
    // Enable all lossy optimizations
    lvtStriping.enabled = true
    sourceFileStriping.enabled = true
    
    // Make the PNG optimization (even though disabled here, shush)
    // Use less optimization and no alpha optimizations
    png.alpha = false
    png.optimizationLevel = 2
}
```

To locate tasks that can be added, use the `dumpTasksWithOutputJars` task, that will automatically list any tasks with output JARs.

### Supported 3rd party plugins

This is a list of currently supported 3rd party plugins that Machete can automatically optimize the output of.
Please see [KnownGoodTasks.kt](https://github.com/P03W/Machete/blob/master/src/main/kotlin/io/github/p03w/machete/util/KnownGoodTasks.kt) for this support.
If you are using a plugin that doesn't specify its output artifacts, I'm sorry, but I can't add support,
please consider poking the developer about using that system (although im sure the code here is garbage as well LMAO,
gradle have good docs challenge (impossible)).

- [shadow](https://github.com/johnrengelman/shadow)
- [fabric-loom](https://github.com/FabricMC/fabric-loom/) and most derivatives
