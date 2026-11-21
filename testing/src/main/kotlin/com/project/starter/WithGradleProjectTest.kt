package com.project.starter

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.io.InputStream

@Suppress("UnnecessaryAbstractClass")
abstract class WithGradleProjectTest {

    @TempDir
    lateinit var rootDirectory: File

    protected fun runTask(vararg taskName: String, shouldFail: Boolean = false, configurationCacheEnabled: Boolean = false): BuildResult =
        GradleRunner.create().apply {
            forwardOutput()
            withPluginClasspath()
            withProjectDir(rootDirectory)

            withArguments(
                buildList {
                    addAll(taskName)
                    add("-Dorg.gradle.unsafe.isolated-projects=true")
                    if (configurationCacheEnabled) {
                        add("--configuration-cache")
                    }
                },
            )
        }.run {
            if (shouldFail) {
                buildAndFail()
            } else {
                build()
            }
        }

    protected fun applySettingsPlugins(vararg pluginIds: String) {
        val settingsFile = rootDirectory.resolve("settings.gradle")
        val block = pluginIds.joinToString(separator = "\n", prefix = "plugins {\n", postfix = "\n}\n\n") { "    id '$it'" }
        settingsFile.writeText(block + settingsFile.takeIf(File::exists)?.readText().orEmpty())
    }

    protected fun File.resolve(relative: String, receiver: File.() -> Unit): File = resolve(relative).apply {
        parentFile.mkdirs()
        receiver()
    }
}
