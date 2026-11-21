package com.project.starter.plugins

import com.project.starter.WithGradleProjectTest
import com.project.starter.kotlinClass
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

internal class CommonSettingsPluginTest : WithGradleProjectTest() {

    lateinit var settingsFile: File
    lateinit var module1Root: File

    @BeforeEach
    fun setUp() {
        rootDirectory.apply {
            settingsFile = resolve("settings.gradle")
            settingsFile.writeText(
                """
                plugins {
                    id('com.starter.config')
                }
                
                include ":module1"
                """.trimIndent(),
            )
            module1Root = resolve("module1") {
                resolve("build.gradle") {
                    writeText(
                        """
                        plugins {
                            id('org.jetbrains.kotlin.jvm')
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
            commonConfig {
                javaVersion = JavaVersion.VERSION_11
                androidPlugin {
                    compileSdkVersion = 37
                    minSdkVersion = 31
                    targetSdkVersion = 37
                }
                qualityPlugin {
                    formatOnCompile = true
                }
            }
            """.trimIndent()
        settingsFile.appendText("\n" + buildscript)

        val result = runTask("help")

        assertThat(result.tasks).noneMatch { it.outcome == TaskOutcome.FAILED }
    }

    @Test
    fun `configures common config extension using function syntax`() {
        // language=groovy
        val buildscript =
            """
            commonConfig {
                javaVersion JavaVersion.VERSION_11
                androidPlugin {
                    compileSdkVersion 37
                    minSdkVersion 31
                    targetSdkVersion 37
                }
                qualityPlugin {
                    formatOnCompile true
                }
            }
            """.trimIndent()
        settingsFile.appendText("\n" + buildscript)

        val result = runTask("help")

        assertThat(result.tasks).noneMatch { it.outcome == TaskOutcome.FAILED }
    }

    @Test
    fun `hands configuration over to every project`() {
        // language=groovy
        val config =
            """
            commonConfig {
                javaVersion = JavaVersion.VERSION_11
                androidPlugin {
                    minSdkVersion = 31
                }
                qualityPlugin {
                    formatOnCompile = true
                }
            }
            """.trimIndent()
        settingsFile.appendText("\n" + config)
        // language=groovy
        val printConfig =
            """

            tasks.register("printConfig") {
                def config = starterCommonConfig
                def summary = "${'$'}path: java=${'$'}{config.javaVersion}, minSdk=${'$'}{config.android.minSdkVersion}, formatOnCompile=${'$'}{config.quality.formatOnCompile}"
                doLast { println(summary) }
            }
            """.trimIndent()
        rootDirectory.resolve("build.gradle").writeText(printConfig)
        module1Root.resolve("build.gradle").appendText("\n" + printConfig)

        val firstRun = runTask("printConfig")
        val secondRun = runTask("printConfig")

        listOf(firstRun, secondRun).forEach { result ->
            assertThat(result.output)
                .contains(":printConfig: java=11, minSdk=31, formatOnCompile=true")
                .contains(":module1:printConfig: java=11, minSdk=31, formatOnCompile=true")
        }
        assertThat(secondRun.output).contains("Reusing configuration cache")
    }
}
