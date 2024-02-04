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

            // https://docs.gradle.org/8.1.1/userguide/configuration_cache.html#config_cache:not_yet_implemented:testkit_build_with_java_agent
            if (!configurationCacheEnabled) {
                withJaCoCo()
            }
        }.run {
            if (shouldFail) {
                buildAndFail()
            } else {
                build()
            }
        }

    private fun GradleRunner.withJaCoCo(): GradleRunner {
        javaClass.classLoader.getResourceAsStream("testkit-gradle.properties")
            ?.toFile(File(projectDir, "gradle.properties"))
        return this
    }

    private fun InputStream.toFile(file: File) {
        use { input ->
            file.outputStream().use { input.copyTo(it) }
        }
    }

    protected fun File.resolve(relative: String, receiver: File.() -> Unit): File = resolve(relative).apply {
        parentFile.mkdirs()
        receiver()
    }
}
