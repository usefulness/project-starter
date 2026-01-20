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

internal class AndroidApplicationPluginTest : WithGradleProjectTest() {

    lateinit var rootBuildScript: File
    lateinit var module1Root: File
    lateinit var module2Root: File

    @BeforeEach
    fun setUp() {
        rootDirectory.apply {
            resolve("settings.gradle").writeText("""include ":module1", ":module2" """)

            rootBuildScript = resolve("build.gradle") {
                writeText(
                    """
                    plugins {
                        id('com.starter.config')
                    }
                    
                    commonConfig {
                        javaVersion = JavaVersion.VERSION_11
                    }
                    """.trimIndent(),
                )
            }
            module1Root = resolve("module1") {
                val buildScript =
                    // language=groovy
                    """
                    plugins {
                        id('com.starter.application.android')
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
                resolve("src/main/AndroidManifest.xml") {
                    writeText(
                        """
                        <manifest />
                        
                        """.trimIndent(),
                    )
                }
                resolve("src/main/kotlin/com/example/ValidKotlinFile1.kt") {
                    writeText(kotlinClass("ValidKotlinFile1"))
                }
                resolve("src/release/kotlin/com/example/ReleaseModel.kt") {
                    writeText(kotlinClass("ReleaseModel"))
                }
                resolve("src/test/kotlin/com/example/Test1.kt") {
                    writeText(kotlinTestClass("Test1"))
                }
            }
            module2Root = resolve("module2") {
                resolve("build.gradle") {
                    writeText(
                        """
                        plugins {
                            id('com.starter.application.android')
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
                resolve("src/main/AndroidManifest.xml") {
                    writeText(
                        """
                        <manifest />
                        
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
    fun `plugin compiles 'src_main_kotlin' classes`() {
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
    fun `configures quality plugin by default`() {
        val qualityEnabled = runTask("projectCodeStyle")

        assertThat(qualityEnabled.task(":module1:projectCodeStyle")?.outcome).isNotNull()
        assertThat(qualityEnabled.task(":module2:projectCodeStyle")?.outcome).isNotNull()
    }
}
