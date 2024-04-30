package com.project.starter.quality

import com.project.starter.WithGradleProjectTest
import com.project.starter.kotlinClass
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

internal class AndroidQualityPluginTest : WithGradleProjectTest() {

    private lateinit var module1Root: File
    private lateinit var module2Root: File

    @BeforeEach
    fun setUp() {
        rootDirectory.apply {
            resolve("settings.gradle").writeText(
                // language=groovy
                """
                plugins {
                    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
                }
                
                dependencyResolutionManagement {
                    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
                    repositories {
                        google()
                        mavenCentral()
                    }
                }
                    
                include ":module1", ":module2"
                
                """.trimIndent(),
            )

            rootDirectory.resolve("build.gradle").writeText(
                // language=groovy
                """
                import org.gradle.api.JavaVersion 
                
                plugins {
                    id('com.starter.config')
                }
                
                commonConfig {
                    javaVersion = JavaVersion.VERSION_11
                }
                """.trimIndent(),
            )
            module1Root = resolve("module1") {
                resolve("build.gradle") {
                    writeText(
                        // language=groovy
                        """
                        plugins {
                            id('com.starter.library.android')
                        }
                        
                        android {
                            namespace "com.example.module1"
                        }
                        
                        """.trimIndent(),
                    )
                }

                resolve("src/main/kotlin/com/example/ValidKotlinFile1.kt") {
                    writeText(kotlinClass("ValidKotlinFile1"))
                }
                resolve("src/test/kotlin/com/example/ValidKotlinTest1.kt") {
                    writeText(kotlinClass("ValidKotlinTest1"))
                }
            }
            module2Root = resolve("module2").apply {
                mkdirs()
                val script =
                    // language=groovy
                    """
                    import org.gradle.api.JavaVersion
                    import org.jetbrains.kotlin.gradle.dsl.KotlinCompile
                    import org.jetbrains.kotlin.gradle.dsl.JvmTarget
                    
                    plugins {
                        id('com.starter.quality')
                        id('com.android.library')
                        id('kotlin-android')
                    }
                    
                    def targetJavaVersion = JavaVersion.VERSION_11
                    android {
                        namespace "com.example.module2"
                        compileSdkVersion 34
                        
                        defaultConfig {
                            minSdkVersion 31
                        }
                        compileOptions {
                            sourceCompatibility = targetJavaVersion
                            targetCompatibility = targetJavaVersion
                        }
                    }
                        
                    kotlin {
                        jvmToolchain(21)
                    }

                    tasks.withType(KotlinCompile).configureEach {
                        compilerOptions.jvmTarget = JvmTarget.@Companion.fromTarget(targetJavaVersion.toString())
                    }
                    
                    """.trimIndent()
                resolve("build.gradle").writeText(script)

                resolve("src/main/kotlin/com/example/ValidKotlinFile2.kt") {
                    writeText(kotlinClass("ValidKotlinFile2"))
                }
                resolve("src/test/kotlin/com/example/ValidKotlinTest2.kt") {
                    writeText(kotlinClass("ValidKotlinTest2"))
                }
            }
        }
    }

    @Test
    fun `projectCodeStyle runs Detekt`() {
        val result = runTask("projectCodeStyle")

        assertThat(result.task(":module1:detekt")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":module2:detekt")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `projectCodeStyle runs ktlint`() {
        val result = runTask("projectCodeStyle")

        assertThat(result.task(":module1:lintKotlinMain")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":module1:lintKotlinTest")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":module2:lintKotlinMain")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":module2:lintKotlinTest")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `formatOnCompile option enables failing builds if code style errors found`() {
        val enableFormatOnCompile = {
            val buildscript =
                // language=groovy
                """
                import org.gradle.api.JavaVersion 
                
                plugins {
                    id('com.starter.config')
                }
                
                commonConfig {
                    javaVersion = JavaVersion.VERSION_17
                    qualityPlugin {
                        formatOnCompile = true
                    }
                }
                """.trimIndent()
            rootDirectory.resolve("build.gradle").writeText(buildscript)
        }

        module1Root.resolve("src/main/kotlin/WrongFileName.kt") {
            writeText(kotlinClass("DifferentClassName"))
        }

        val formatOnCompileOff = runTask("assemble")

        assertThat(formatOnCompileOff.task(":module1:formatKotlin")?.outcome).isNull()
        assertThat(formatOnCompileOff.task(":module2:formatKotlin")?.outcome).isNull()

        enableFormatOnCompile()
        val formatOnCompileOn = runTask("assemble")

        assertThat(formatOnCompileOn.task(":module1:formatKotlin")?.outcome).isNotNull()
        assertThat(formatOnCompileOn.task(":module2:formatKotlin")?.outcome).isNotNull()
    }

    @Test
    fun `detekt fails on invalid class name`() {
        module2Root.resolve("src/main/kotlin/com/example/MagicNumber.kt") {
            val kotlinClass =
                // language=kotlin
                """
                class invalidClassName {
                    var value: Int = 16
                }
                
                """.trimIndent()
            writeText(kotlinClass)
        }

        val result = runTask("projectCodeStyle", shouldFail = true)

        assertThat(result.task(":module2:detekt")?.outcome).isEqualTo(TaskOutcome.FAILED)
    }
}
