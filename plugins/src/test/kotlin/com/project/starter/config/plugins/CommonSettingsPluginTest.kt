package com.project.starter.config.plugins

import com.project.starter.WithGradleTest
import java.io.File
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.intellij.lang.annotations.Language
import org.junit.Before
import org.junit.Test

internal class CommonSettingsPluginTest : WithGradleTest() {

    lateinit var rootBuildScript: File
    lateinit var module1Root: File

    @Before
    fun setUp() {
        rootDirectory.apply {
            mkdirs()
            resolve("settings.gradle").writeText("""include ":module1", ":module2" """)

            rootBuildScript = resolve("build.gradle")
            module1Root = resolve("module1").apply {
                mkdirs()
                resolve("build.gradle").writeText("""
                        plugins {
                            id('kotlin')
                        }
                      
                    """.trimIndent())
                resolve("src/main/kotlin/ValidKotlinFile1.kt").apply {
                    parentFile.mkdirs()
                    writeText("""
                            data class ValidKotlinFile1(val name: String)
                            
                        """.trimIndent())
                }
            }
        }
    }

    @Test
    fun `configures common config extension`() {
        @Language("groovy") val buildscript = """
            plugins {
                id('plugin-config')
            }
            
            commonConfig {
                javaVersion = JavaVersion.VERSION_1_8
                javaFilesAllowed = false
                androidPlugin {
                    compileSdkVersion = 29
                    minSdkVersion = 23
                    targetSdkVersion = 29
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
    fun `throws exception if not applied to the root project`() {
        @Language("groovy") val buildscript = """
            plugins {
                id('plugin-config')
            }
        """.trimIndent()
        module1Root.resolve("build.gradle").appendText(buildscript)

        val result = runTask("build", shouldFail = true)

        assertThat(result.output).contains("Failed to apply plugin [id 'plugin-config']")
    }
}
