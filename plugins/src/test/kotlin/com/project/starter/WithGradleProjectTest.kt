package com.project.starter

import java.io.File
import java.io.InputStream
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.ConfigConstants.CONFIG_BRANCH_SECTION
import org.eclipse.jgit.lib.ConfigConstants.CONFIG_KEY_MERGE
import org.eclipse.jgit.lib.ConfigConstants.CONFIG_KEY_REMOTE
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.transport.URIish
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
    private lateinit var origin: File

    @Before
    fun setup() {
        rootDirectory = folder.newFolder().apply {
            mkdirs()
        }
        origin = folder.newFolder("origin").apply {
            mkdirs()
            Git.init().setDirectory(this).call()
        }
        git = Git.init().apply {
            setDirectory(rootDirectory)
        }.call()
        git.remoteAdd().apply {
            setName("origin")
            setUri(URIish(origin.toURI().toURL()))
        }.call()
        git.repository.config.apply {
            val branchName = "master"
            val remoteName = "origin"
            setString(CONFIG_BRANCH_SECTION, branchName, CONFIG_KEY_REMOTE, remoteName)
            setString(CONFIG_BRANCH_SECTION, branchName, CONFIG_KEY_MERGE, Constants.R_HEADS + branchName)
        }.save()
        rootDirectory.resolve(".gitignore").writeText("""
            .gradle
            **/build/
            """.trimIndent())
        commit("init")
        tag("release/1.1.0")
        git.push().apply {
            remote = "origin"
            setPushTags()
            setPushAll()
        }.call()
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
