package com.project.starter.config.plugins

import com.project.starter.WithGradleProjectTest
import com.project.starter.kotlinClass
import java.io.File
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

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
                            id('org.jetbrains.kotlin.jvm') version "1.5.0"
                        }
                      
                        """.trimIndent()
                    )
                }
                resolve("src/main/kotlin/ValidKotlinFile1.kt") {
                    writeText(kotlinClass("ValidKotlinFile1"))
                }
            }
        }
    }

    @Test
    fun `configures common config extension using property syntax`() {
        @Language("groovy")
        val buildscript =
            """
            plugins {
                id('com.starter.config')
            }
            
            commonConfig {
                javaVersion = JavaVersion.VERSION_1_8
                javaFilesAllowed = false
                androidPlugin {
                    compileSdkVersion = 30
                    minSdkVersion = 23
                    targetSdkVersion = 30
                }
                qualityPlugin {
                    formatOnCompile = true
                }
                versioningPlugin {
                    enabled = true
                }
            }
            """.trimIndent()
        rootBuildScript.appendText(buildscript)

        val result = runTask("help")

        assertThat(result.tasks).noneMatch { it.outcome == TaskOutcome.FAILED }
    }

    @Test
    fun `configures common config extension using function syntax`() {
        @Language("groovy")
        val buildscript =
            """
            plugins {
                id('com.starter.config')
            }
            
            commonConfig {
                javaVersion JavaVersion.VERSION_1_8
                javaFilesAllowed false
                androidPlugin {
                    compileSdkVersion 30
                    minSdkVersion 23
                    targetSdkVersion 30
                }
                qualityPlugin {
                    formatOnCompile true
                }
                versioningPlugin {
                    enabled true
                }
            }
            """.trimIndent()
        rootBuildScript.appendText(buildscript)

        val result = runTask("help")

        assertThat(result.tasks).noneMatch { it.outcome == TaskOutcome.FAILED }
    }

    @Test
    fun `throws exception if not applied to the root project`() {
        @Language("groovy")
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
