package com.project.starter.modules

import com.project.starter.WithGradleTest
import com.project.starter.javaClass
import java.io.File
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Before
import org.junit.Test

internal class KotlinLibraryPluginTest : WithGradleTest() {

    lateinit var rootBuildScript: File
    lateinit var module1Root: File
    lateinit var module2Root: File

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
                            id('plugin-library.kotlin')
                        }
                        
                        dependencies {
                            testImplementation 'junit:junit:4.13'
                        }
                        
                    """.trimIndent())
                resolve("src/main/kotlin/ValidKotlinFile1.kt").apply {
                    parentFile.mkdirs()
                    writeText("""
                            data class ValidKotlinFile1(val name: String)
                            
                        """.trimIndent())
                }
                resolve("src/test/kotlin/Test1.kt").apply {
                    parentFile.mkdirs()
                    writeText("""
                            class Test1 {
                            
                                @org.junit.Test
                                fun test1() = Unit
                            }
                            
                        """.trimIndent())
                }
            }
            module2Root = resolve("module2").apply {
                mkdirs()
                resolve("build.gradle").writeText("""
                        plugins {
                            id('plugin-library.kotlin')
                        }
                        
                        dependencies {
                            testImplementation 'junit:junit:4.13'
                        }
                        
                    """.trimIndent())
                resolve("src/main/kotlin/ValidKotlinFile2.kt").apply {
                    parentFile.mkdirs()
                    writeText("""
                            data class ValidKotlinFile2(val name: String)
                            
                        """.trimIndent())
                }
                resolve("src/test/kotlin/Test2.kt").apply {
                    parentFile.mkdirs()
                    writeText("""
                            class Test2 {
                            
                                @org.junit.Test
                                fun test2() = Unit
                            }
                            
                        """.trimIndent())
                }
            }
        }
    }

    @Test
    fun `kotlin library plugin compiles 'src_main_kotlin' classes`() {
        val result = runTask("assemble")

        assertThat(result.task(":module1:assemble")!!.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":module2:assemble")!!.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `projectTest runs tests for all modules`() {
        val result = runTask("projectTest")

        assertThat(result.task(":module1:test")!!.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":module2:test")!!.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(module1Root.resolve("build/test-results/test")).isDirectoryContaining {
            it.name.startsWith("TEST-")
        }
    }

    @Test
    fun `projectCoverage runs coverage for all modules`() {
        val result = runTask("projectCoverage")

        assertThat(result.task(":module1:test")!!.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":module2:test")!!.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(module1Root.resolve("build/reports/jacoco/test")).isDirectoryContaining {
            it.name == "jacocoTestReport.xml"
        }
    }

    @Test
    fun `projectLint runs coverage for all modules`() {
        module1Root.resolve("build.gradle").appendText("""
            apply plugin: "com.android.lint"
        """.trimIndent())
        module2Root.resolve("build.gradle").appendText("""
            apply plugin: "com.android.lint"
        """.trimIndent())

        val result = runTask("projectLint")

        assertThat(result.task(":module1:lint")!!.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":module2:lint")!!.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `does not fail on java files by default`() {
        module2Root.resolve("src/main/java/JavaClass.java").apply {
            parentFile.mkdirs()
            writeText(javaClass("JavaClass"))
        }

        val result = runTask(":module2:assemble")

        assertThat(result.task(":module2:assemble")!!.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `fail on java files if failing enabled`() {
        module2Root.resolve("build.gradle").appendText("""
            libraryConfig {
                javaFilesAllowed = false
            }
        """.trimIndent())
        module2Root.resolve("src/main/java/JavaClass.java").apply {
            parentFile.mkdirs()
            writeText(javaClass("JavaClass"))
        }

        val result = runTask(":module2:assemble", shouldFail = true)

        assertThat(result.task(":module2:forbidJavaFiles")!!.outcome).isEqualTo(TaskOutcome.FAILED)
    }
}
