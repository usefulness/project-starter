package com.project.starter.modules

import com.project.starter.WithGradleProjectTest
import com.project.starter.javaClass
import com.project.starter.kotlinClass
import com.project.starter.kotlinTestClass
import java.io.File
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.intellij.lang.annotations.Language
import org.junit.Before
import org.junit.Test

internal class KotlinLibraryPluginTest : WithGradleProjectTest() {

    lateinit var rootBuildScript: File
    lateinit var module1Root: File
    lateinit var module2Root: File

    @Before
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
                        
                        """.trimIndent()
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
                        
                        """.trimIndent()
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
    fun `projectLint runs coverage for all modules`() {
        module1Root.resolve("build.gradle").appendText(
            """
            apply plugin: "com.android.lint"
            """.trimIndent()
        )
        module2Root.resolve("build.gradle").appendText(
            """
            apply plugin: "com.android.lint"
            """.trimIndent()
        )

        val result = runTask("projectLint")

        assertThat(result.task(":module1:lint")!!.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":module2:lint")!!.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `does not fail on java files by default`() {
        module2Root.resolve("src/main/java/JavaClass.java") {
            writeText(javaClass("JavaClass"))
        }

        val result = runTask(":module2:assemble")

        assertThat(result.task(":module2:assemble")!!.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `fail on java files if failing enabled`() {
        module2Root.resolve("build.gradle").appendText(
            """
            projectConfig {
                javaFilesAllowed = false
            }
            """.trimIndent()
        )
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
    fun `does not configure quality plugin if disabled using configuration plugin`() {
        @Language("groovy")
        val qualityScript =
            """
            plugins {
                id('com.starter.config')
            }
            
            commonConfig {
                qualityPlugin {
                    enabled = false
                }
            }
            """.trimIndent()
        rootBuildScript.appendText(qualityScript)

        val qualityDisabled = runTask("projectCodeStyle", shouldFail = true)

        assertThat(qualityDisabled.output).contains("Task 'projectCodeStyle' not found ")
    }

    @Test
    fun `configures versioning plugin by default`() {
        tag("release/1.2.2")
        commit("random commit")

        val versioningEnabled = runTask("currentVersion")

        assertThat(versioningEnabled.output).contains("version: 1.2.3-SNAPSHOT")
    }

    @Test
    fun `does not configure versioning plugin if disabled using configuration plugin`() {
        @Language("groovy")
        val versioningScript =
            """
            plugins {
                id('com.starter.config')
            }
            
            commonConfig {
                versioningPlugin {
                    enabled = false
                }
            }
            """.trimIndent()
        rootBuildScript.appendText(versioningScript)

        val versioningDisabled = runTask("currentVersion", shouldFail = true)

        assertThat(versioningDisabled.output).contains("Task 'currentVersion' not found ")
    }
}
