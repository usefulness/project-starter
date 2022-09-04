package com.project.starter.modules

import com.project.starter.WithGradleProjectTest
import com.project.starter.commit
import com.project.starter.javaClass
import com.project.starter.kotlinClass
import com.project.starter.kotlinTestClass
import com.project.starter.setupGit
import com.project.starter.tag
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class KotlinLibraryPluginTest : WithGradleProjectTest() {

    lateinit var rootBuildScript: File
    lateinit var module1Root: File
    lateinit var module2Root: File

    @TempDir
    lateinit var origin: File

    @BeforeEach
    fun setUp() {
        rootDirectory.apply {
            mkdirs()
            resolve("settings.gradle").writeText("""include ":module1", ":module2" """)

            rootBuildScript = resolve("build.gradle")
            module1Root = resolve("module1") {
                resolve("build.gradle") {
                    writeText(
                        """
                        plugins {
                            id('com.starter.library.kotlin')
                        }
                        
                        dependencies {
                            testImplementation 'junit:junit:4.13'
                        }
                        
                        """.trimIndent(),
                    )
                }
                resolve("src/main/kotlin/ValidKotlinFile1.kt") {
                    writeText(kotlinClass("ValidKotlinFile1"))
                }
                resolve("src/test/kotlin/Test1.kt") {
                    writeText(kotlinTestClass("Test1"))
                }
            }
            module2Root = resolve("module2") {
                resolve("build.gradle") {
                    writeText(
                        """
                        plugins {
                            id('com.starter.library.kotlin')
                        }
                        
                        dependencies {
                            testImplementation 'junit:junit:4.13'
                        }
                        
                        """.trimIndent(),
                    )
                }
                resolve("src/main/kotlin/ValidKotlinFile2.kt") {
                    writeText(kotlinClass("ValidKotlinFile2"))
                }
                resolve("src/test/kotlin/Test2.kt") {
                    writeText(kotlinTestClass("Test2"))
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
    fun `does not fail on java files if failing disabled`() {
        module2Root.resolve("build.gradle").appendText(
            """
            projectConfig {
                javaFilesAllowed = true
            }
            """.trimIndent(),
        )
        module2Root.resolve("src/main/java/JavaClass.java") {
            writeText(javaClass("JavaClass"))
        }

        val result = runTask(":module2:assemble")

        assertThat(result.task(":module2:assemble")!!.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `fails on java files by default`() {
        module2Root.resolve("src/main/java/JavaClass.java") {
            writeText(javaClass("JavaClass"))
        }

        val result = runTask(":module2:assemble", shouldFail = true)

        assertThat(result.task(":module2:forbidJavaFiles")!!.outcome).isEqualTo(TaskOutcome.FAILED)
    }

    @Test
    fun `configures quality plugin by default`() {
        val qualityEnabled = runTask("projectCodeStyle")

        assertThat(qualityEnabled.task(":module1:projectCodeStyle")?.outcome).isNotNull()
        assertThat(qualityEnabled.task(":module2:projectCodeStyle")?.outcome).isNotNull()
    }

    @Test
    fun `configures versioning plugin by default`() {
        rootBuildScript.writeText(
            // language=groovy
            """
            plugins {
                id 'com.starter.versioning'
            }
            """.trimIndent(),
        )
        val git = setupGit(origin)
        git.tag("v1.2.2")
        git.commit("random commit")

        val versioningEnabled = runTask("currentVersion")

        assertThat(versioningEnabled.output).contains("version: 1.3.0-SNAPSHOT")
    }
}
