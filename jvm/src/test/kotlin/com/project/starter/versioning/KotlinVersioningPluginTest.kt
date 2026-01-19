package com.project.starter.versioning

import com.project.starter.WithGradleProjectTest
import com.project.starter.commit
import com.project.starter.setupGit
import com.project.starter.tag
import org.assertj.core.api.Assertions.assertThat
import org.eclipse.jgit.api.Git
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

internal class KotlinVersioningPluginTest : WithGradleProjectTest() {

    private lateinit var module1Root: File
    private lateinit var module2Root: File
    private lateinit var rootBuildScript: File
    private lateinit var git: Git

    @BeforeEach
    fun setUp() {
        rootDirectory.apply {
            resolve("settings.gradle").writeText("""include ":module1", ":module2" """)
            rootBuildScript = resolve("build.gradle")
            module1Root = resolve("module1") {
                resolve("build.gradle") {
                    writeText(
                        """
                        plugins {
                            id 'com.starter.library.kotlin'
                        }
                        """.trimIndent(),
                    )
                }
            }
            module2Root = resolve("module2") {
                resolve("build.gradle") {
                    writeText(
                        """
                        plugins {
                            id 'com.starter.library.kotlin'
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
    fun `sets version to all projects`() {
        rootBuildScript.writeText(
            // language=groovy
            """
            plugins {
                id 'com.starter.versioning'
            }
            """.trimIndent(),
        )
        git.commit("features in 1.2.0")
        git.tag("v1.2.0")

        val modules = listOf(":module1", ":module2", "")

        modules.forEach {
            val moduleResult = runTask("$it:properties")

            assertThat(moduleResult.output).contains("version: 1.2.0")
        }
    }
}
