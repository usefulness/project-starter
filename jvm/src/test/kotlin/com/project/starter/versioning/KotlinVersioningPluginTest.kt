package com.project.starter.versioning

import com.project.starter.WithGradleProjectTest
import com.project.starter.commit
import com.project.starter.setupGit
import com.project.starter.tag
import org.assertj.core.api.Assertions.assertThat
import org.eclipse.jgit.api.Git
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class KotlinVersioningPluginTest : WithGradleProjectTest() {

    private lateinit var module1Root: File
    private lateinit var module2Root: File
    private lateinit var git: Git

    @TempDir
    lateinit var origin: File

    @BeforeEach
    fun setUp() {
        rootDirectory.apply {
            resolve("settings.gradle").writeText("""include ":module1", ":module2" """)
            module1Root = resolve("module1") {
                resolve("build.gradle") {
                    writeText(
                        """
                        plugins {
                            id 'com.starter.library.kotlin'
                        }
                        """.trimIndent()
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
                        """.trimIndent()
                    )
                }
            }
        }
        git = setupGit(origin)
        git.tag("release-1.1.0")
    }

    @Test
    fun `sets version to all projects`() {
        git.commit("features in 1.2.0")
        git.tag("release-1.2.0")

        val modules = listOf(":module1", ":module2", "")

        modules.forEach {
            val moduleResult = runTask("$it:properties")

            assertThat(moduleResult?.output).contains("version: 1.2.0")
        }
    }
}
