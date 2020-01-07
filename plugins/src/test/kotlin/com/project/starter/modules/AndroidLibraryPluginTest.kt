package com.project.starter.modules

import com.project.starter.WithGradleTest
import com.project.starter.javaClass
import java.io.File
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.intellij.lang.annotations.Language
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

internal class AndroidLibraryPluginTest : WithGradleTest() {

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
                @Language("groovy") val buildScript = """
                        plugins {
                            id('com.starter.library.android')
                        }
                        
                        android {
                            buildTypes {
                                debug { }
                                superType { }
                                release { }
                            }
                            flavorDimensions "version"
                            productFlavors {
                                demo { }
                                full { }
                            }
                        }
                        
                        dependencies {
                            testImplementation 'junit:junit:4.13'
                        }
                        
                    """.trimIndent()
                resolve("build.gradle").writeText(buildScript)
                resolve("src/main/AndroidManifest.xml").apply {
                    parentFile.mkdirs()
                    writeText("""
                        <manifest package="com.example.module1" />
                        
                    """.trimIndent())
                }
                resolve("src/main/kotlin/ValidKotlinFile1.kt").apply {
                    parentFile.mkdirs()
                    writeText("""
                            data class ValidKotlinFile1(val name: String)
                            
                        """.trimIndent())
                }
                resolve("src/release/kotlin/ReleaseModel.kt").apply {
                    parentFile.mkdirs()
                    writeText("""
                            data class ReleaseModel(val name: String)
                            
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
                            id('com.starter.library.android')
                        }
                        
                        dependencies {
                            testImplementation 'junit:junit:4.13'
                        }
                        
                    """.trimIndent())
                resolve("src/main/AndroidManifest.xml").apply {
                    parentFile.mkdirs()
                    writeText("""
                        <manifest package="com.example.module1" />
                        
                    """.trimIndent())
                }
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

        assertThat(result.task(":module1:assemble")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":module2:assemble")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `projectTest runs tests for all modules`() {
        val result = runTask("projectTest")

        assertThat(result.task(":module1:testDemoDebugUnitTest")!!.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":module2:testDebugUnitTest")!!.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(module1Root.resolve("build/test-results/testDemoDebugUnitTest")).isDirectoryContaining {
            it.name.startsWith("TEST-")
        }
        assertThat(module2Root.resolve("build/test-results/testDebugUnitTest")).isDirectoryContaining {
            it.name.startsWith("TEST-")
        }
    }

    @Test
    @Ignore("Android coverage not implemented yet")
    fun `projectCoverage runs coverage for all modules`() {
        val result = runTask("projectCoverage")

        assertThat(result.task(":module1:test")!!.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":module2:test")!!.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(module1Root.resolve("build/reports/jacoco/test")).isDirectoryContaining {
            it.name == "jacocoTestReport.xml"
        }
    }

    @Test
    fun `does not contain BuildConfig file if generation disabled`() {
        val result = runTask("assembleDebug")

        assertThat(result.task(":module2:assembleDebug")!!.outcome).isEqualTo(TaskOutcome.SUCCESS)
        val generated = module2Root.resolve("build/generated/")
        assertThat(generated.walk()).matches({ allFiles ->
            allFiles.none { it.isFile && it.name == "BuildConfig.java" }
        }, "BuildConfig file found in $generated")
    }

    @Test
    fun `contains BuildConfig file if generation enabled`() {
        @Language("groovy") val config = """
            projectConfig {
                generateBuildConfig = true
            }
            
        """.trimIndent()
        module2Root.resolve("build.gradle").appendText(config)

        val result = runTask("assembleDebug")

        assertThat(result.task(":module2:assembleDebug")!!.outcome).isEqualTo(TaskOutcome.SUCCESS)
        val generated = module2Root.resolve("build/generated/")
        assertThat(generated.walk()).matches({ allFiles ->
            allFiles.any { it.isFile && it.name == "BuildConfig.java" }
        }, "BuildConfig file not found in $generated")
    }

    @Test
    fun `configures projectXXX tasks when default variants provided`() {
        @Language("groovy") val config = """
            projectConfig {
                defaultVariants = ["demoDebug", "fullRelease"]
            }
            
        """.trimIndent()
        module1Root.resolve("build.gradle").appendText(config)

        val result = runTask("module1:projectTest", "module1:projectLint", "module1:projectCoverage")

        assertThat(result.task(":module1:testDemoDebugUnitTest")!!.outcome).isNotEqualTo(TaskOutcome.FAILED)
        assertThat(result.task(":module1:testFullReleaseUnitTest")!!.outcome).isNotEqualTo(TaskOutcome.FAILED)
        assertThat(result.task(":module1:lintDemoDebug")!!.outcome).isNotEqualTo(TaskOutcome.FAILED)
        assertThat(result.task(":module1:lintFullRelease")!!.outcome).isNotEqualTo(TaskOutcome.FAILED)
        assertThat(result.task(":module1:jacocoDemoDebugTestReport")!!.outcome).isNotEqualTo(TaskOutcome.FAILED)
        assertThat(result.task(":module1:jacocoFullReleaseTestReport")!!.outcome).isNotEqualTo(TaskOutcome.FAILED)
    }

    @Test
    fun `configures android library extension`() {
        @Language("groovy") val config = """
            projectConfig {
                generateBuildConfig = false
                javaFilesAllowed = false
                defaultVariants = ["demoDebug", "fullRelease"]
                coverageExclusions = ["**/view/**"]
            }
            
        """.trimIndent()
        module1Root.resolve("build.gradle").appendText(config)

        runTask("help")
    }

    @Test
    fun `does not fail on java sources by default`() {
        module2Root.resolve("src/main/java/JavaAllowed.java").apply {
            parentFile.mkdirs()
            writeText(javaClass(className = "JavaAllowed"))
        }

        val result = runTask(":module2:assembleDebug")

        assertThat(result.task(":module2:assembleDebug")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `fail on java files if settings enabled at project level`() {
        @Language("groovy") val config = """
            projectConfig {
                javaFilesAllowed = false
            }
            
        """.trimIndent()
        module2Root.resolve("build.gradle").appendText(config)
        module2Root.resolve("src/main/java/JavaFile.java").apply {
            parentFile.mkdirs()
            writeText(javaClass(className = "JavaFile"))
        }

        val result = runTask(":module2:assembleDebug", shouldFail = true)

        assertThat(result.task(":module2:forbidJavaFiles")?.outcome).isEqualTo(TaskOutcome.FAILED)
    }
}
