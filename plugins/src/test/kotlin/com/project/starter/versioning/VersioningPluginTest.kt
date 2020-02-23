package com.project.starter.versioning

import com.project.starter.WithGradleProjectTest
import org.assertj.core.api.Assertions.assertThat
import org.eclipse.jgit.api.CreateBranchCommand.SetupUpstreamMode
import org.eclipse.jgit.api.ListBranchCommand.ListMode
import org.eclipse.jgit.lib.BranchConfig
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

internal class VersioningPluginTest : WithGradleProjectTest() {

    private lateinit var module1Root: File
    private lateinit var module2Root: File

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
                            id 'kotlin'
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
                            id 'kotlin'
                        }
                        """.trimIndent()
                    )
                }
            }
        }
        tag("release-1.1.0")
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
        commit("features in 1.2.0")
        tag("release-1.2.0")

        val modules = listOf(":module1", ":module1", "")

        modules.forEach {
            val moduleResult = runTask("$it:properties")

            assertThat(moduleResult?.output).contains("version: 1.2.0")
        }
    }

    @Test
    fun `goes regular release flow`() {
        tag("release-1.2.0")
        commit("contains 1.3.0 features")

        assertThat(runTask("currentVersion").output).contains("1.3.0-SNAPSHOT")

        commit("contains 1.3.0 features")

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
        commit("contains 1.2.0 changes")
        assertThat(runTask("currentVersion").output).contains("1.2.0-SNAPSHOT")

        git.push().call()
        runTask("release")
        assertThat(runTask("currentVersion").output).contains("1.2.0")

        checkout("release/1.2.0")
        commit("contains 1.2.1 fix")
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
        commit("features in 1.2.0")
        tag("1.2.0")

        val modules = listOf(":module1", ":module1", "")

        modules.forEach {
            val moduleResult = runTask("$it:properties")

            assertThat(moduleResult?.output).contains("version: 1.2.0")
        }
    }
}
