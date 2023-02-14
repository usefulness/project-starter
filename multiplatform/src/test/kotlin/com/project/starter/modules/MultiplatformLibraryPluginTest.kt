package com.project.starter.modules

import com.project.starter.WithGradleProjectTest
import com.project.starter.commit
import com.project.starter.kotlinClass
import com.project.starter.kotlinMultiplatformTestClass
import com.project.starter.setupGit
import com.project.starter.tag
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

internal class MultiplatformLibraryPluginTest : WithGradleProjectTest() {

    lateinit var rootBuildScript: File
    lateinit var module1Root: File
    lateinit var module2Root: File

    @TempDir
    lateinit var origin: File

    @BeforeEach
    fun setUp() {
        rootDirectory.apply {
            mkdirs()
            resolve("settings.gradle").writeText(
                // language=groovy
                """
                    include ":module1", ":module2"
                """.trimIndent(),
            )

            rootBuildScript = resolve("build.gradle")
            module1Root = resolve("module1") {
                resolve("build.gradle") {
                    writeText(
                        // language=groovy
                        """
                            plugins {
                                id('com.starter.library.multiplatform')
                            }
                            
                            kotlin {
                                jvm()
                                
                                sourceSets {
                                    commonTest {
                                        dependencies {
                                            implementation kotlin("test")
                                        }
                                    }
                                }
                            }
                        
                        """.trimIndent(),
                    )
                }
                resolve("src/commonMain/kotlin/com/example/ValidKotlinFile1.kt") {
                    writeText(kotlinClass("ValidKotlinFile1"))
                }
                resolve("src/jvmTest/kotlin/com/example/JvmTest1.kt") {
                    writeText(kotlinMultiplatformTestClass("JvmTest1"))
                }
            }
            module2Root = resolve("module2") {
                resolve("build.gradle") {
                    writeText(
                        // language=groovy
                        """
                            plugins {
                                id('com.starter.library.multiplatform')
                            }
                            
                            kotlin {
                                jvm()
                                ios()
                                
                                sourceSets {
                                    commonTest {
                                        dependencies {
                                            implementation kotlin("test")
                                        }
                                    }
                                }
                            }
                        
                        """.trimIndent(),
                    )
                }
                resolve("src/commonMain/kotlin/com/example/ValidKotlinFile1.kt") {
                    writeText(kotlinClass("ValidKotlinFile1"))
                }
                resolve("src/commonTest/kotlin/com/example/ValidKotlinTest1.kt") {
                    writeText(kotlinMultiplatformTestClass("ValidKotlinTest1"))
                }
                resolve("src/jvmMain/kotlin/com/example/ValidKotlinJvmFile1.kt") {
                    writeText(kotlinClass("ValidKotlinJvmFile1"))
                }
                resolve("src/jvmTest/kotlin/com/example/ValidJvmKotlinTest1.kt") {
                    writeText(kotlinMultiplatformTestClass("ValidJvmKotlinTest1"))
                }
                resolve("src/iosMain/kotlin/com/example/ValidKotlinIosFile1.kt") {
                    writeText(kotlinClass("ValidKotlinIosFile1"))
                }
                resolve("src/iosTest/kotlin/com/example/ValidIosKotlinTest1.kt") {
                    writeText(kotlinMultiplatformTestClass("ValidIosKotlinTest1"))
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

        assertThat(result.task(":module1:allTests")!!.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":module2:allTests")!!.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `projectCoverage runs coverage for all modules`() {
        val result = runTask("projectCoverage")

        assertThat(result.task(":module1:jvmTest")!!.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":module2:jvmTest")!!.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(module1Root.resolve("build/reports/jacoco/jacocoTestReport")).isDirectoryContaining {
            it.name == "jacocoTestReport.xml"
        }
    }

    @Test
    fun `configures quality plugin by default`() {
        val qualityEnabled = runTask("projectCodeStyle")

        assertThat(qualityEnabled.task(":module1:projectCodeStyle")?.outcome).isNotNull()
        assertThat(qualityEnabled.task(":module2:projectCodeStyle")?.outcome).isNotNull()
    }

    @Test
    fun `configures versioning plugin when applied`() {
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
