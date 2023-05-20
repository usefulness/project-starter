package com.project.starter.versioning

import com.project.starter.WithGradleProjectTest
import com.project.starter.checkout
import com.project.starter.commit
import com.project.starter.setupGit
import com.project.starter.tag
import org.assertj.core.api.Assertions.assertThat
import org.eclipse.jgit.api.Git
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

internal class VersioningPluginTest : WithGradleProjectTest() {

    private lateinit var module1Root: File
    private lateinit var module2Root: File
    private lateinit var git: Git

    @BeforeEach
    fun setUp() {
        rootDirectory.apply {
            resolve("settings.gradle").writeText("""include ":module1", ":module2" """)

            resolve("build.gradle").writeText(
                """
                plugins {
                    id 'com.starter.versioning'
                }
                
                """.trimIndent(),
            )
            module1Root = resolve("module1") {
                resolve("build.gradle") {
                    writeText(
                        """
                        plugins {
                            id 'org.jetbrains.kotlin.jvm' version "1.8.21"
                        }
                        """.trimIndent(),
                    )
                }
            }
            module2Root = resolve("module1") {
                resolve("build.gradle") {
                    writeText(
                        """
                        plugins {
                            id 'org.jetbrains.kotlin.jvm' version "1.8.21"
                        }
                        """.trimIndent(),
                    )
                }
            }
        }
        git = setupGit()
        git.tag("v1.1.0")
    }

    @Test
    fun `fails if not applied to root project`() {
        module1Root.resolve("build.gradle").writeText(
            """
            apply plugin: "com.starter.versioning"
            
            """.trimIndent(),
        )

        val result = runTask("help", shouldFail = true)

        assertThat(result?.output).contains("Versioning plugin can be applied to the root project only")
    }

    @Test
    fun `sets version to all projects`() {
        git.commit("features in 2.11.1234")
        git.tag("v2.11.1234")

        val modules = listOf(":module1", ":module1", "")

        modules.forEach {
            val moduleResult = runTask("$it:properties")

            assertThat(moduleResult?.output).contains("version: 2.11.1234")
        }
    }

    @Test
    fun `when multiple tags on the same commit`() {
        git.tag("v1.2.1")
        assertThat(runTask("currentVersion").output).contains("1.2.1")

        git.commit("test commit")
        assertThat(runTask("currentVersion").output).contains("1.3.0-SNAPSHOT")

        git.tag("v1.2.123")
        git.tag("v1.3.0")
        git.tag("v1.2.146")
        assertThat(runTask("currentVersion").output).contains("1.3.0")

        git.commit("after all the mess")
        assertThat(runTask("currentVersion").output).contains("1.4.0-SNAPSHOT")
    }

    @Test
    fun `regular release flow`() {
        assertThat(runTask("currentVersion").output).contains("1.1.0")

        git.commit("contains 1.2.0 features")
        assertThat(runTask("currentVersion").output).contains("1.2.0-SNAPSHOT")

        git.tag("v1.2.0")
        assertThat(runTask("currentVersion").output).contains("1.2.0")

        git.commit("contains 1.3.0 features")
        assertThat(runTask("currentVersion").output).contains("1.3.0-SNAPSHOT")

        git.commit("contains another set of 1.3.0 features")
        assertThat(runTask("currentVersion").output).contains("1.3.0-SNAPSHOT")

        git.tag("v1.2.1")
        assertThat(runTask("currentVersion").output).contains("1.2.1")

        git.commit("contains 1.3.0 features")
        assertThat(runTask("currentVersion").output).contains("1.3.0-SNAPSHOT")

        git.checkout("v1.2.0")
        assertThat(runTask("currentVersion").output).contains("1.2.0")
    }

    @Test
    fun `version on branch`() {
        git.commit("contains 1.2.3 features")
        git.tag("v1.2.3")

        git.branchCreate().setName("testBranch").call()
        git.checkout("testBranch")
        assertThat(runTask("currentVersion").output).contains("1.2.3")

        git.commit("we're on a branch")
        assertThat(runTask("currentVersion").output).contains("1.3.0-SNAPSHOT")

        git.checkout("master")
        assertThat(runTask("currentVersion").output).contains("1.2.3")

        git.commit("contains 1.2.4 features")
        git.tag("v1.2.4")
        assertThat(runTask("currentVersion").output).contains("1.2.4")

        git.checkout("testBranch")
        assertThat(runTask("currentVersion").output).contains("1.3.0-SNAPSHOT")
    }

    @Test
    fun configurationCacheCompatibility() {
        git.commit("contains 1.2.0 features")
        git.tag("v1.2.0")
        git.commit("contains 1.3.0 features")

        val result = runTask("currentVersion", configurationCacheEnabled = true)
        assertThat(result.task(":currentVersion")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.output)
            .contains("Calculating task graph as no configuration cache is available for tasks")
            .contains("1.3.0-SNAPSHOT")

        val result2 = runTask("currentVersion", configurationCacheEnabled = true)
        assertThat(result2.task(":currentVersion")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result2.output)
            .contains("Reusing configuration cache")
            .contains("1.3.0-SNAPSHOT")
    }
}
