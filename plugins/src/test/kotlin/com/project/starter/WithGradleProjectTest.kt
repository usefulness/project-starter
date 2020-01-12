package com.project.starter

import java.io.File
import java.io.InputStream
import org.eclipse.jgit.api.Git
import org.gradle.testkit.runner.GradleRunner
import org.junit.Before
import org.junit.Rule
import org.junit.rules.TemporaryFolder

internal abstract class WithGradleProjectTest {

    @get:Rule
    var folder = TemporaryFolder()

    lateinit var git: Git
        private set
    lateinit var rootDirectory: File
        private set

    @Before
    fun setup() {
        rootDirectory = folder.newFolder().apply {
            mkdirs()
        }
        git = Git.init().apply {
            setDirectory(rootDirectory)
        }.call()
        rootDirectory.resolve(".gitignore").writeText("""
            .gradle
            **/build/
            """.trimIndent())
        commit("init")
        tag("release/1.1.0")
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

    protected fun commit(commitMessage: String) {
        rootDirectory.resolve("File.txt").appendText("""
            | Text
            """.trimMargin())
        git.add().apply {
            addFilepattern(".")
        }.call()
        git.commit().apply {
            setAll(true)
            message = commitMessage
        }.call()
    }

    protected fun tag(tagName: String) {
        git.tag().apply {
            name = tagName
            isAnnotated = false
        }.call()
    }
}
