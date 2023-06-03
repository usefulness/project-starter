package com.project.starter.plugins

import com.project.starter.WithGradleProjectTest
import com.project.starter.kotlinClass
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

internal class CommonSettingsPluginTest : WithGradleProjectTest() {

    lateinit var rootBuildScript: File
    lateinit var module1Root: File

    @BeforeEach
    fun setUp() {
        rootDirectory.apply {
            resolve("settings.gradle").writeText("""include ":module1" """)

            rootBuildScript = resolve("build.gradle")
            module1Root = resolve("module1") {
                resolve("build.gradle") {
                    writeText(
                        """
                        plugins {
                            id('org.jetbrains.kotlin.jvm') version "1.8.20"
                        }
                      
                        """.trimIndent(),
                    )
                }
                resolve("src/main/kotlin/com/example/ValidKotlinFile1.kt") {
                    writeText(kotlinClass("ValidKotlinFile1"))
                }
            }
        }
    }

    @Test
    fun `configures common config extension using property syntax`() {
        // language=groovy
        val buildscript =
            """
            plugins {
                id('com.starter.config')
            }
            
            commonConfig {
                javaVersion = JavaVersion.VERSION_11
                javaFilesAllowed = false
                androidPlugin {
                    compileSdkVersion = 30
                    minSdkVersion = 23
                    targetSdkVersion = 30
                }
                qualityPlugin {
                    formatOnCompile = true
                }
            }
            """.trimIndent()
        rootBuildScript.appendText(buildscript)

        val result = runTask("help")

        assertThat(result.tasks).noneMatch { it.outcome == TaskOutcome.FAILED }
    }

    @Test
    fun `configures common config extension using function syntax`() {
        // language=groovy
        val buildscript =
            """
            plugins {
                id('com.starter.config')
            }
            
            commonConfig {
                javaVersion JavaVersion.VERSION_11
                javaFilesAllowed false
                androidPlugin {
                    compileSdkVersion 30
                    minSdkVersion 23
                    targetSdkVersion 30
                }
                qualityPlugin {
                    formatOnCompile true
                }
            }
            """.trimIndent()
        rootBuildScript.appendText(buildscript)

        val result = runTask("help")

        assertThat(result.tasks).noneMatch { it.outcome == TaskOutcome.FAILED }
    }

    @Test
    fun `throws exception if not applied to the root project`() {
        // language=groovy
        val buildscript =
            """
            plugins {
                id('com.starter.config')
            }
            """.trimIndent()
        module1Root.resolve("build.gradle").appendText(buildscript)

        val result = runTask("build", shouldFail = true)

        assertThat(result.output).contains("Failed to apply plugin 'com.starter.config'")
    }
}
