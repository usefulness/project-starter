package com.project.starter.modules

import com.project.starter.WithGradleProjectTest
import com.project.starter.javaClass
import com.project.starter.kotlinClass
import com.project.starter.kotlinTestClass
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

internal class AndroidLibraryPluginTest : WithGradleProjectTest() {

    lateinit var rootBuildScript: File
    lateinit var module1Root: File
    lateinit var module2Root: File

    @BeforeEach
    fun setUp() {
        rootDirectory.apply {
            mkdirs()
            resolve("settings.gradle").writeText("""include ":module1", ":module2" """)

            rootBuildScript = resolve("build.gradle")
            module1Root = resolve("module1") {
                val buildScript =
                    // language=groovy
                    """
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
                resolve("build.gradle") {
                    writeText(buildScript)
                }
                resolve("src/main/AndroidManifest.xml") {
                    writeText(
                        """
                        <manifest package="com.example.module1" />
                        
                        """.trimIndent(),
                    )
                }
                resolve("src/main/kotlin/ValidKotlinFile1.kt") {
                    writeText(kotlinClass("ValidKotlinFile1"))
                }
                resolve("src/release/kotlin/ReleaseModel.kt") {
                    writeText(kotlinClass("ReleaseModel"))
                }
                resolve("src/test/kotlin/Test1.kt") {
                    writeText(
                        // language=kotlin
                        """
                        package com.example
                        
                        class Test1 {
                        
                            @org.junit.Test
                            fun someTest() = Unit
                        }
                        
                        """.trimIndent(),
                    )
                }
            }
            module2Root = resolve("module2") {
                resolve("build.gradle") {
                    writeText(
                        """
                        plugins {
                            id('com.starter.library.android')
                        }
                        
                        dependencies {
                            testImplementation 'junit:junit:4.13'
                        }
                        
                        """.trimIndent(),
                    )
                }
                resolve("src/main/AndroidManifest.xml") {
                    writeText(
                        """
                        <manifest package="com.example.module1" />
                        
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
    fun `filters out unnecessary build variants`() {
        val result = runTask(":module2:assemble", "-m")

        assertThat(result.output).doesNotContain(":module2:assembleRelease")
    }

    @Test
    fun `projectCoverage runs coverage for all modules`() {
        val result = runTask("projectCoverage")

        assertThat(result.task(":module1:testDemoDebugUnitTest")!!.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":module2:testDebugUnitTest")!!.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":module2:testReleaseUnitTest")).isNull()
        println(module1Root.resolve("build/reports/").list()?.toList().toString())
        assertThat(module1Root.resolve("build/reports/jacoco/jacocoDemoDebugTestReport")).isDirectoryContaining {
            it.name.startsWith("jacoco") && it.name.endsWith(".xml")
        }
        assertThat(module2Root.resolve("build/reports/jacoco/jacocoDebugTestReport")).isDirectoryContaining {
            it.name.startsWith("jacoco") && it.name.endsWith(".xml")
        }
    }

    @Test
    fun `does not contain BuildConfig file if generation disabled`() {
        val result = runTask("assembleDebug")

        assertThat(result.task(":module2:assembleDebug")!!.outcome).isEqualTo(TaskOutcome.SUCCESS)
        val generated = module2Root.resolve("build/generated/")
        assertThat(generated.walk()).matches(
            { allFiles ->
                allFiles.none { it.isFile && it.name == "BuildConfig.java" }
            },
            "BuildConfig file found in $generated",
        )
    }

    @Test
    fun `configures projectXXX tasks when default variants provided`() {
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
        val config =
            // language=groovy
            """
            projectConfig {
                javaFilesAllowed = false
                coverageExclusions = ["**/view/**"]
            }
            
            """.trimIndent()
        module1Root.resolve("build.gradle").appendText(config)

        runTask("help")
    }

    @Test
    fun `does not fail on java sources by default`() {
        module2Root.resolve("src/main/java/JavaAllowed.java") {
            writeText(javaClass(className = "JavaAllowed"))
        }

        val result = runTask(":module2:assembleDebug")

        assertThat(result.task(":module2:assembleDebug")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `fail on java files if settings enabled at project level`() {
        val config =
            // language=groovy
            """
            projectConfig {
                javaFilesAllowed = false
            }
            
            """.trimIndent()
        module2Root.resolve("build.gradle").appendText(config)
        module2Root.resolve("src/main/java/JavaFile.java") {
            writeText(javaClass(className = "JavaFile"))
        }

        val result = runTask(":module2:assembleDebug", shouldFail = true)

        assertThat(result.task(":module2:forbidJavaFiles")?.outcome).isEqualTo(TaskOutcome.FAILED)
    }

    @Test
    fun `configures quality plugin by default`() {
        val qualityEnabled = runTask("projectCodeStyle")

        assertThat(qualityEnabled.task(":module1:projectCodeStyle")?.outcome).isNotNull()
        assertThat(qualityEnabled.task(":module2:projectCodeStyle")?.outcome).isNotNull()
    }
}
