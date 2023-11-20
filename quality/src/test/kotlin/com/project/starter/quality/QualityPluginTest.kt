package com.project.starter.quality

import com.project.starter.WithGradleProjectTest
import com.project.starter.javaClass
import com.project.starter.kotlinClass
import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class QualityPluginTest : WithGradleProjectTest() {

    @BeforeEach
    fun setUp() {
        rootDirectory.apply {
            resolve("build.gradle") {
                // language=groovy
                writeText(
                    """
                    import org.jetbrains.kotlin.gradle.dsl.KotlinCompile
                    import org.jetbrains.kotlin.gradle.dsl.JvmTarget
                    
                    plugins {
                        id('com.starter.quality')
                        id('org.jetbrains.kotlin.jvm')
                    }
            
                    def targetJavaVersion = JavaVersion.VERSION_11
                    tasks.withType(JavaCompile).configureEach {
                        options.release.set(targetJavaVersion.majorVersion.toInteger())
                    }
                    tasks.withType(KotlinCompile).configureEach {
                        compilerOptions.jvmTarget = JvmTarget.@Companion.fromTarget(targetJavaVersion.toString())
                    }
                    
                    repositories.mavenCentral()
                    """.trimIndent(),
                )
            }
            resolve("src/main/kotlin/com/example/ValidKotlinFile1.kt") {
                writeText(kotlinClass("ValidKotlinFile1"))
            }
            resolve("src/main/java/ValidJava1.java") {
                writeText(javaClass("ValidJava1"))
            }
            resolve("src/debug/java/DebugJava.java") {
                writeText(javaClass("DebugJava"))
            }
            resolve("src/test/kotlin/com/example/ValidKotlinTest1.kt") {
                writeText(kotlinClass("ValidKotlinTest1"))
            }
            resolve("src/test/java/com/example/ValidJavaTest1.java") {
                writeText(javaClass("ValidJavaTest1"))
            }
        }
    }

    @Test
    fun `projectCodeStyle runs Detekt`() {
        val result = runTask("projectCodeStyle")

        assertThat(result.task(":detekt")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `projectCodeStyle runs ktlint`() {
        val result = runTask("projectCodeStyle")

        assertThat(result.task(":lintKotlinMain")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(result.task(":lintKotlinTest")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `formatOnCompile option enables failing builds if code style errors found`() {
        val enableFormatOnCompile = {
            // language=groovy
            val buildscript =
                """
                import org.gradle.api.JavaVersion
                import org.jetbrains.kotlin.gradle.dsl.KotlinCompile
                import org.jetbrains.kotlin.gradle.dsl.JvmTarget
                
                plugins {
                    id('com.starter.config')
                    id('com.starter.quality')
                    id('org.jetbrains.kotlin.jvm')
                }
                
                commonConfig {
                    qualityPlugin {
                        formatOnCompile true
                    }
                }
                        
                kotlin {
                    jvmToolchain(21)
                }
                
                def targetJavaVersion = JavaVersion.VERSION_11
                tasks.withType(JavaCompile).configureEach {
                    options.release.set(targetJavaVersion.majorVersion.toInteger())
                }
                tasks.withType(KotlinCompile).configureEach {
                    compilerOptions.jvmTarget = JvmTarget.@Companion.fromTarget(targetJavaVersion.toString())
                }
                """.trimIndent()
            rootDirectory.resolve("build.gradle").writeText(buildscript)
        }

        rootDirectory.resolve("src/main/kotlin/WrongFileName.kt") {
            writeText(kotlinClass("DifferentClassName"))
        }

        val formatOnCompileOff = runTask("assemble")

        assertThat(formatOnCompileOff.task(":formatKotlin")?.outcome).isNull()

        enableFormatOnCompile()
        val formatOnCompileOn = runTask("assemble")

        assertThat(formatOnCompileOn.task(":formatKotlin")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `detekt fails on invalid class name`() {
        rootDirectory.resolve("src/main/kotlin/MagicNumber.kt") {
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

        assertThat(result.task(":detekt")?.outcome).isEqualTo(TaskOutcome.FAILED)
    }
}
