# Machete

Machete is a gradle plugin that attempts to optimize the size of output JARs
through both individual file optimizations and overall compression increases inspired by the [Detonater](https://github.com/EnnuiL/Detonater) project.

Simply applying the plugin should be enough to apply any optimizations in a standard environment,
as it will collect output JARs that are known to be usable artifacts and optimize them after the `build` task.

### Optimizations

- JSON files are minimized by reading+writing through GSON, which strips any whitespace.
- PNG files are run through the [Oxipng](https://github.com/shssoichiro/oxipng) project on maximum compression
- Nested JAR files are unpacked and have the same optimizations run on them
- The final result is them compressed with DEFLATE level 9, providing modest overall compression (bytecode doesn't compress well unfortunately)

More optimizations are planned as well, such as XML minification


### Configuration

To configure the plugin, use the `machete` block. This allows you to

- Add or remove tasks to pull output JARs from (`additionalTasks`/`ignoredTasks`, also please consider opening a PR if you use these)
- Add or remove JAR files to optimize (`additionalJars`/`ignoredJars`)
- Disable overwriting the original artifacts (`keepOriginal`)

To locate tasks that can be added, use the `dumpTasksWithOutputJars` task, that will automatically list any tasks with output JARs.

### Supported 3rd party plugins

This is a list of currently supported 3rd party plugins that Machete can automatically optimize the output of.
Please see [KnownGoodTasks.kt](https://github.com/P03W/Machete/blob/master/src/main/kotlin/io/github/p03w/machete/util/KnownGoodTasks.kt) for this support.
If you are using a plugin that doesn't specify its output artifacts, I'm sorry, but I can't add support,
please consider poking the developer about using proper gradle plugin development techniques.

- [fabric-loom](https://github.com/FabricMC/fabric-loom/) and most derivatives
