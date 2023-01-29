package com.project.starter.modules.tasks

import com.project.starter.WithGradleProjectTest
import com.project.starter.javaClass
import com.project.starter.kotlinClass
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

internal class ForbidJavaFilesTaskTest : WithGradleProjectTest() {

    lateinit var main: File
    lateinit var test: File

    @BeforeEach
    fun setUp() {
        rootDirectory.apply {
            mkdirs()
            resolve("settings.gradle").writeText("""include ":module1", ":parentModule:childModule" """)

            resolve("build.gradle")
            resolve("module1") {
                val buildScript =
                    // language=groovy
                    """
                    plugins {
                        id('com.starter.library.android')
                    }
                    
                    android {
                        namespace "com.example.module1"
                    }
                    
                    projectConfig {
                        javaFilesAllowed = false
                    }
                    
                    android {
                        namespace "com.example.module1"
                    }
                    """.trimIndent()
                resolve("build.gradle") {
                    writeText(buildScript)
                }
                main = resolve("src/main") {
                    resolve("kotlin/com/example/ValidKotlin.kt") {
                        writeText(kotlinClass("ValidKotlin"))
                    }
                    resolve("java/com/example/KotlinInJavaDir.kt") {
                        writeText(kotlinClass("KotlinInJavaDir"))
                    }
                }
                test = resolve("src/test") {
                    resolve("kotlin/Test1.kt") {
                        writeText(kotlinClass("Test1"))
                    }
                }
            }

            resolve("parentModule") {
                val parentBuildScript =
                    // language=groovy
                    """
                    plugins {
                        id('com.starter.library.android')
                    }
                    
                    android {
                        namespace "com.example.parent"
                    }
                    """.trimIndent()
                resolve("build.gradle") {
                    writeText(parentBuildScript)
                }
                resolve("src/main") {
                    resolve("kotlin/ValidKotlinInParent.kt") {
                        writeText(kotlinClass("ValidKotlinInParent"))
                    }
                }
                resolve("childModule") {
                    val childBuildscript =
                        // language=groovy
                        """
                        plugins {
                            id('com.starter.library.kotlin')
                        }
                        
                        projectConfig {
                            javaFilesAllowed = false
                        }
                        """.trimIndent()
                    resolve("build.gradle") {
                        writeText(childBuildscript)
                    }
                    resolve("src/main") {
                        resolve("kotlin/ValidKotlinInChild.kt") {
                            writeText(kotlinClass("ValidKotlinInChild"))
                        }
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
        main.resolve("java/JavaClass.java") {
            writeText(javaClass("JavaClass"))
        }

        val result = runTask("assemble", shouldFail = true)

        assertThat(result.task(":module1:forbidJavaFiles")?.outcome).isEqualTo(TaskOutcome.FAILED)
        assertThat(result.output).contains("Java files are not allowed within :module1")
    }

    @Test
    fun `task fails on test sources`() {
        test.resolve("java/JavaTest.java") {
            writeText(javaClass("JavaTest"))
        }

        val result = runTask("assemble", shouldFail = true)

        assertThat(result.task(":module1:forbidJavaFiles")?.outcome).isEqualTo(TaskOutcome.FAILED)
        assertThat(result.output).contains("Java files are not allowed within :module1")
    }

    @Test
    fun `task is cacheable`() {
        runTask(":module1:assemble")

        val secondRun = runTask(":module1:assemble")

        assertThat(secondRun.task(":module1:forbidJavaFiles")?.outcome).isNotEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `doesn't check generated files`() {
        rootDirectory.resolve("build/generated/source/apollo/classes/main/JavaTest.java") {
            writeText(javaClass("JavaTest"))
        }

        val secondRun = runTask("assemble")

        assertThat(secondRun.task(":module1:forbidJavaFiles")?.outcome).isNotEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `does not fail if registered in nested non-android module with android parent`() {
        rootDirectory.resolve("parentModule/childModule/src/main/java/JavaClass.java") {
            writeText(javaClass("JavaClass"))
        }

        val result = runTask(":parentModule:childModule:forbidJavaFiles", shouldFail = true)

        assertThat(result.task(":parentModule:childModule:forbidJavaFiles")?.outcome).isEqualTo(TaskOutcome.FAILED)
    }
}
