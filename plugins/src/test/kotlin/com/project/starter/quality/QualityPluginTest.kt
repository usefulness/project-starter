package com.project.starter.quality

import com.project.starter.WithGradleTest
import java.io.File
import kotlin.test.Test
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.intellij.lang.annotations.Language
import org.junit.Before

internal class QualityPluginTest : WithGradleTest() {

    private lateinit var module1Root: File
    private lateinit var module2Root: File

    @Before
    fun setUp() {
        rootDirectory.apply {
            resolve("settings.gradle").writeText("""include ":module1", ":module2" """)

            resolve("build.gradle").writeText("""
            """.trimIndent())
            module1Root = resolve("module1").apply {
                mkdirs()
                resolve("build.gradle").writeText("""
                    plugins {
                        id('plugin-quality')
                        id('kotlin')
                    }
                """.trimIndent())
                resolve("src/main/kotlin/ValidKotlinFile1.kt").apply {
                    parentFile.mkdirs()
                    writeText("""
                        object ValidKotlinFile1
                        
                    """.trimIndent())
                }
                resolve("src/test/kotlin/ValidKotlinTest1.kt").apply {
                    parentFile.mkdirs()
                    writeText("""
                        object ValidKotlinTest1
                        
                    """.trimIndent())
                }
            }
            module2Root = resolve("module2").apply {
                mkdirs()
                resolve("build.gradle").writeText("""
                    plugins {
                        id('plugin-quality')
                        id('com.android.library')
                        id('kotlin-android')
                    }
                    
                    repositories {
                        google()
                    }
                    
                    android {
                        compileSdkVersion 29
                        defaultConfig {
                            minSdkVersion 23
                        }
                    }
                    
                """.trimIndent())
                resolve("src/main/AndroidManifest.xml").apply {
                    parentFile.mkdirs()
                    writeText("""
                         <manifest package="com.example.module2" />
                    """.trimIndent())
                }
                resolve("src/main/java/ValidKotlinFile2.kt").apply {
                    parentFile.mkdirs()
                    writeText("""
                        object ValidKotlinFile2
                        
                    """.trimIndent())
                }
                resolve("src/test/java/ValidKotlinTest2.kt").apply {
                    parentFile.mkdirs()
                    writeText("""
                        object ValidKotlinTest2
                        
                    """.trimIndent())
                }
            }
        }
    }

    @Test
    fun `projectCodeStyle runs Detekt`() {
        val result = runTask("projectCodeStyle")

        assertThat(result.task(":module1:detekt")!!.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":module2:detekt")!!.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `projectCodeStyle runs ktlint`() {
        val result = runTask("projectCodeStyle")

        assertThat(result.task(":module1:lintKotlinMain")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":module1:lintKotlinTest")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":module2:lintKotlinMain")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":module2:lintKotlinTest")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `formatOnCompile option enables failing builds if code style errors found`() {
        @Language("groovy") val buildscript = """
            plugins {
                id('plugin-config')
            }
            
            commonConfig {
                qualityPlugin {
                    formatOnCompile = true
                }
            }
        """.trimIndent()
        rootDirectory.resolve("build.gradle").appendText(buildscript)
        module1Root.resolve("src/main//kotlin/WrongFileName.kt").apply {
            parentFile.mkdirs()
            writeText("""
                object DifferentClassName
                
            """.trimIndent())
        }

        val result = runTask("assemble")

        assertThat(result.task(":module1:formatKotlin")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":module2:formatKotlin")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }
}
