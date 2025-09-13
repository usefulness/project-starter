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

    protected fun File.resolve(relative: String, receiver: File.() -> Unit): File = resolve(relative).apply {
        parentFile.mkdirs()
        receiver()
    }
}
