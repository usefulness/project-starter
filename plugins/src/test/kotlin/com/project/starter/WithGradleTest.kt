package com.project.starter

import java.io.File
import java.io.InputStream
import org.gradle.testkit.runner.GradleRunner
import org.junit.Before
import org.junit.Rule
import org.junit.rules.TemporaryFolder

internal abstract class WithGradleTest {

    @get:Rule
    var folder = TemporaryFolder()

    lateinit var rootDirectory: File
        private set

    @Before
    fun setup() {
        rootDirectory = folder.newFolder().apply {
            mkdirs()
        }
    }

    protected fun runTask(vararg taskName: String, shouldFail: Boolean = false) =
        GradleRunner.create().apply {
            forwardOutput()
            withPluginClasspath()
            withArguments(*taskName)
            withProjectDir(rootDirectory)
            withJaCoCo()
        }.run {
            if (shouldFail) {
                buildAndFail()
            } else {
                build()
            }
        }

    fun GradleRunner.withJaCoCo(): GradleRunner {
        javaClass.classLoader.getResourceAsStream("testkit-gradle.properties")
            ?.toFile(File(projectDir, "gradle.properties"))
        return this
    }

    fun InputStream.toFile(file: File) {
        use { input ->
            file.outputStream().use { input.copyTo(it) }
        }
    }
}
