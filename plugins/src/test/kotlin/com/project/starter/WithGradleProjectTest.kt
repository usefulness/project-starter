package com.project.starter

import org.eclipse.jgit.api.CreateBranchCommand
import org.eclipse.jgit.api.CreateBranchCommand.SetupUpstreamMode
import java.io.File
import java.io.InputStream
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.ConfigConstants.CONFIG_BRANCH_SECTION
import org.eclipse.jgit.lib.ConfigConstants.CONFIG_KEY_MERGE
import org.eclipse.jgit.lib.ConfigConstants.CONFIG_KEY_REMOTE
import org.eclipse.jgit.lib.Constants
import org.eclipse.jgit.transport.URIish
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.io.TempDir

internal abstract class WithGradleProjectTest {

    @TempDir
    lateinit var rootDirectory: File

    @TempDir
    lateinit var origin: File

    lateinit var git: Git
        private set

    @BeforeEach
    fun setup() {
        Git.init().setDirectory(origin).call()
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
        rootDirectory.resolve(".gitignore").writeText(
            """
            .gradle
            **/build/
            """.trimIndent()
        )
        commit("init")
        git.push().apply {
            remote = "origin"
            setPushTags()
            setPushAll()
        }.call()
    }

    @AfterEach
    fun tearDown() {
        git.close()
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

    protected fun checkout(branchName: String){
        git.checkout().apply {
            setName(branchName)
        }.call()
        git.repository.config.apply {
            val remoteName = "origin"
            setString(CONFIG_BRANCH_SECTION, branchName, CONFIG_KEY_REMOTE, remoteName)
            setString(CONFIG_BRANCH_SECTION, branchName, CONFIG_KEY_MERGE, Constants.R_HEADS + branchName)
        }.save()
    }

    protected fun commit(commitMessage: String) {
        rootDirectory.resolve("File.txt").appendText(
            """
            | Text
            """.trimMargin()
        )
        git.add().apply {
            addFilepattern(".")
        }.call()
        git.commit().apply {
            setAll(true)
            setSign(false)
            message = commitMessage
        }.call()
    }

    protected fun tag(tagName: String) {
        git.tag().apply {
            name = tagName
            isAnnotated = false
            isSigned = false
        }.call()
    }

    protected fun File.resolve(relative: String, receiver: File.() -> Unit): File =
        resolve(relative).apply {
            parentFile.mkdirs()
            receiver()
        }
}
