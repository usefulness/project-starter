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

            rootBuildScript = resolve("build.gradle") {
                writeText(
                    """
                    plugins {
                        id('com.starter.config')
                    }
                    
                    commonConfig {
                        javaVersion = JavaVersion.VERSION_1_8 // workaround for http://issuetracker.google.com/issues/294137077
                    }
                    """.trimIndent(),
                )
            }
            module1Root = resolve("module1") {
                val buildScript =
                    // language=groovy
                    """
                    plugins {
                        id('com.starter.library.android')
                    }
                    
                    android {
                        namespace "com.example.module1"
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
                        testImplementation 'junit:junit:4.13.2'
                    }
                    
                    """.trimIndent()
                resolve("build.gradle") {
                    writeText(buildScript)
                }

                resolve("src/main/kotlin/com/example/ValidKotlinFile1.kt") {
                    writeText(kotlinClass("ValidKotlinFile1"))
                }
                resolve("src/release/kotlin/com/example/ReleaseModel.kt") {
                    writeText(kotlinClass("ReleaseModel"))
                }
                resolve("src/test/kotlin/com/example/Test1.kt") {
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
                        
                        android {
                            namespace "com.example.module2"
                        }
                        
                        dependencies {
                            testImplementation 'junit:junit:4.13.2'
                        }
                        
                        """.trimIndent(),
                    )
                }

                resolve("src/main/kotlin/com/example/ValidKotlinFile2.kt") {
                    writeText(kotlinClass("ValidKotlinFile2"))
                }
                resolve("src/test/kotlin/com/example/Test2.kt") {
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
        val result = runTask("module1:projectTest", "module1:projectLint")

        assertThat(result.task(":module1:testDemoDebugUnitTest")!!.outcome).isNotEqualTo(TaskOutcome.FAILED)
        assertThat(result.task(":module1:testFullReleaseUnitTest")!!.outcome).isNotEqualTo(TaskOutcome.FAILED)
        assertThat(result.task(":module1:lintDemoDebug")!!.outcome).isNotEqualTo(TaskOutcome.FAILED)
        assertThat(result.task(":module1:lintFullRelease")!!.outcome).isNotEqualTo(TaskOutcome.FAILED)
    }

    @Test
    fun `configures quality plugin by default`() {
        val qualityEnabled = runTask("projectCodeStyle")

        assertThat(qualityEnabled.task(":module1:projectCodeStyle")?.outcome).isNotNull()
        assertThat(qualityEnabled.task(":module2:projectCodeStyle")?.outcome).isNotNull()
    }
}
