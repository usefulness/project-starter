package com.project.starter.modules.tasks

import com.project.starter.WithGradleProjectTest
import com.project.starter.javaClass
import java.io.File
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.intellij.lang.annotations.Language
import org.junit.Before
import org.junit.Test

internal class ForbidJavaFilesTaskTest : WithGradleProjectTest() {

    lateinit var main: File
    lateinit var test: File

    @Before
    fun setUp() {
        rootDirectory.apply {
            mkdirs()
            resolve("settings.gradle").writeText("""include ":module1", ":module2" """)

            resolve("module1").apply {
                mkdirs()
                @Language("groovy")
                val buildScript = """
                plugins {
                    id('com.starter.library.kotlin')
                }
                
                projectConfig {
                    javaFilesAllowed = false
                }
            """.trimIndent()
                resolve("build.gradle").writeText(buildScript)
                main = resolve("src/main").apply {
                    parentFile.mkdirs()
                    resolve("kotlin/ValidKotlin.kt").apply {
                        parentFile.mkdirs()
                        writeText("""
                            object ValidKotlin
                            
                        """.trimIndent())
                    }
                    resolve("java/KotlinInJavaDir.kt").apply {
                        parentFile.mkdirs()
                        writeText("""
                            object KotlinInJavaDir
                            
                        """.trimIndent())
                    }
                }
                test = resolve("src/test").apply {
                    parentFile.mkdirs()
                    resolve("kotlin/Test1.kt").apply {
                        parentFile.mkdirs()
                        writeText("""
                            object Test1
                            
                        """.trimIndent())
                    }
                }
            }
        }
    }

    @Test
    fun `task passes configuration phase`() {
        runTask("help")
    }

    @Test
    fun `task fails on main sources`() {
        main.resolve("java/JavaClass.java").apply {
            parentFile.mkdirs()
            writeText(javaClass("JavaClass"))
        }

        val result = runTask("assemble", shouldFail = true)

        assertThat(result.task(":module1:forbidJavaFiles")?.outcome).isEqualTo(TaskOutcome.FAILED)
        assertThat(result.output).contains("Java files are not allowed within :module1")
    }

    @Test
    fun `task fails on test sources`() {
        test.resolve("java/JavaTest.java").apply {
            parentFile.mkdirs()
            writeText(javaClass("JavaTest"))
        }

        val result = runTask("assemble", shouldFail = true)

        assertThat(result.task(":module1:forbidJavaFiles")?.outcome).isEqualTo(TaskOutcome.FAILED)
        assertThat(result.output).contains("Java files are not allowed within :module1")
    }

    @Test
    fun `task is cacheable`() {
        runTask("assemble")

        val secondRun = runTask("assemble")

        assertThat(secondRun.task(":module1:forbidJavaFiles")?.outcome).isNotEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `doesn't check generated files`() {
        rootDirectory.resolve("build/generated/source/apollo/classes/main/JavaTest.java").apply {
            parentFile.mkdirs()
            writeText(javaClass("JavaTest"))
        }

        val secondRun = runTask("assemble")

        assertThat(secondRun.task(":module1:forbidJavaFiles")?.outcome).isNotEqualTo(TaskOutcome.SUCCESS)
    }
}
