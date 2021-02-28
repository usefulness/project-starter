package com.project.starter.versioning

import com.project.starter.WithGradleProjectTest
import com.project.starter.checkout
import com.project.starter.commit
import com.project.starter.setupGit
import com.project.starter.tag
import java.io.File
import org.assertj.core.api.Assertions.assertThat
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.ListBranchCommand.ListMode
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

internal class VersioningPluginTest : WithGradleProjectTest() {

    private lateinit var module1Root: File
    private lateinit var module2Root: File
    private lateinit var git: Git

    @TempDir
    lateinit var origin: File

    @BeforeEach
    fun setUp() {
        rootDirectory.apply {
            resolve("settings.gradle").writeText("""include ":module1", ":module2" """)

            resolve("build.gradle").writeText(
                """
                plugins {
                    id 'com.starter.versioning'
                }
                
                """.trimIndent()
            )
            module1Root = resolve("module1") {
                resolve("build.gradle") {
                    writeText(
                        """
                        plugins {
                            id 'org.jetbrains.kotlin.jvm' version "1.4.31"
                        }
                        """.trimIndent()
                    )
                }
            }
            module2Root = resolve("module1") {
                resolve("build.gradle") {
                    writeText(
                        """
                        plugins {
                            id 'org.jetbrains.kotlin.jvm' version "1.4.31"
                        }
                        """.trimIndent()
                    )
                }
            }
        }
        git = setupGit(origin)
        git.tag("release-1.1.0")
    }

    @Test
    fun `fails if not applied to root project`() {
        module1Root.resolve("build.gradle").writeText(
            """
            apply plugin: "com.starter.versioning"
            
            """.trimIndent()
        )

        val result = runTask("help", shouldFail = true)

        assertThat(result?.output).contains("Versioning plugin can be applied to the root project only")
    }

    @Test
    fun `sets version to all projects`() {
        git.commit("features in 1.2.0")
        git.tag("release-1.2.0")

        val modules = listOf(":module1", ":module1", "")

        modules.forEach {
            val moduleResult = runTask("$it:properties")

            assertThat(moduleResult?.output).contains("version: 1.2.0")
        }
    }

    @Test
    fun `goes regular release flow`() {
        git.tag("release-1.2.0")
        git.commit("contains 1.3.0 features")

        assertThat(runTask("currentVersion").output).contains("1.3.0-SNAPSHOT")

        git.commit("contains 1.3.0 features")

        assertThat(runTask("currentVersion").output).contains("1.3.0-SNAPSHOT")

        git.push().call()
        runTask("release")

        assertThat(runTask("currentVersion").output).contains("1.3.0")
        val branches = git.branchList().apply {
            setListMode(ListMode.ALL)
        }.call()
        assertThat(branches.map { it.name }).contains("refs/remotes/origin/release/1.3.0")
    }

    @Test
    fun `goes regular flow on release branch`() {
        assertThat(runTask("currentVersion").output).contains("1.1.0")
        git.commit("contains 1.2.0 changes")
        assertThat(runTask("currentVersion").output).contains("1.2.0-SNAPSHOT")

        git.push().call()
        runTask("release")
        assertThat(runTask("currentVersion").output).contains("1.2.0")

        git.checkout("release/1.2.0")
        git.commit("contains 1.2.1 fix")
        assertThat(runTask("currentVersion").output).contains("1.2.1-SNAPSHOT")

        git.push().call()
        runTask("release")
        assertThat(runTask("currentVersion").output).contains("1.2.1")
    }

    @Test
    fun `can override axion config`() {
        rootDirectory.resolve("build.gradle") {
            appendText(
                """
                scmVersion {
                    tag {
                        prefix = ""
                        versionSeparator = ""
                    }
                }
                
                """.trimIndent()
            )
        }
        git.commit("features in 1.2.0")
        git.tag("1.2.0")

        val modules = listOf(":module1", ":module1", "")

        modules.forEach {
            val moduleResult = runTask("$it:properties")

            assertThat(moduleResult?.output).contains("version: 1.2.0")
        }
    }
}
